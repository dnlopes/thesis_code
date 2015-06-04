from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run, warn_only
import time
import sys
import xml.etree.ElementTree as ET
from sets import Set
import logging
import shlex
import subprocess, signal
import os
import configParser as config

logger = logging.getLogger('simple_example')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

################################################################################################
#   LOCAL VARIABLES
################################################################################################

env.shell = "/bin/bash -l -i -c" 
env.user = config.user
env_vars = dict()
env.hosts = ['localhost']

################################################################################################
#   START COMPONENTS METHODS
################################################################################################

@parallel
def startDatabasesGalera(isMaster):
    mysqlCommand = ''
    clusterAddress = generateClusterAddress()

    mysqlCommand = config.MYSQL_START_COMMAND + ' --wsrep_retry_autocommit=0 --wsrep_log_conflicts=on --wsrep_debug=on --wsrep_cluster_address="' + clusterAddress + '"'
    if isMaster:
        mysqlCommand += " --wsrep-new-cluster"

    command = 'nohup ' + mysqlCommand + ' >& /dev/null < /dev/null &'

    logger.info('starting database at %s', env.host_string)
    logger.info(command)
    with cd(config.GALERA_MYSQL_DIR), hide('running','output'):    
        run(command)    
    
    time.sleep(30)   

    if not isPortOpen(config.MYSQL_PORT):
        return '0'
    return '1'

@parallel
def startDatabases():
    command = 'nohup ' + config.MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &'  
    logger.info('starting database at %s', env.host_string)
    logger.info(command)
    with cd(config.MYSQL_DIR), hide('running','output'):    
        run(command)    
    
    time.sleep(30)

    if not isPortOpen(config.MYSQL_PORT):
        return '0'
    return '1'

@parallel
def startCoordinators(configFile):
    currentId = config.coordinators_map.get(env.host_string)    
    port = config.coordinatorsIdToPortMap.get(currentId)
    logFile = 'coordinator' + str(currentId) + '.log'
    command = 'java -Xms2000m -Xmx4000m -jar coordinator.jar ' + configFile + ' ' + str(currentId) + ' > ' + logFile + ' &'
    if config.IS_LOCALHOST:
        command = 'java -jar coordinator.jar ' + configFile + ' ' + str(currentId) + ' > ' + logFile + ' &'

    logger.info('starting coordinator at %s', env.host_string)
    logger.info('%s',command)
    with cd(config.DEPLOY_DIR), hide('running','output'):
        run(command)
    
    if config.IS_LOCALHOST == True:
        time.sleep(20)
    else:
        time.sleep(10)
    
    time.sleep(10)
    if not isPortOpen(port):
        return '0'
    return '1'

@parallel
def startReplicators(configFile):
    currentId = config.replicators_map.get(env.host_string)    
    port = config.replicatorsIdToPortMap.get(currentId)
    logFile = 'replicator' + str(currentId) + '.log'
    command = 'java -Xms2000m -Xmx4000m -jar replicator.jar ' + configFile + ' ' + str(currentId) + ' > ' + logFile + ' &'
    if config.IS_LOCALHOST:
        command = 'java -jar replicator.jar ' + configFile + ' ' + str(currentId) + ' > ' + logFile + ' &'
        
    logger.info('starting replicator at %s', env.host_string)
    logger.info('%s',command)
    with cd(config.DEPLOY_DIR), hide('running','output'):
        run(command)
    
    if config.IS_LOCALHOST == True:
        time.sleep(20)
    else:
        time.sleep(20)

    time.sleep(10)
    if not isPortOpen(port):
        return '0'
    return '1'

def startTPCCclients(configFile, proxiesNumber, usersPerProxy, useCustomJDBC):
    jarFile = ''
    if config.TPCC_VERSION == 'escada':
        jarFile = 'tpcc-escada-client.jar'
    else:
        jarFile = 'tpcc-codefuture-client.jar'

    for y in xrange(1, proxiesNumber+1):
        currentId = str(y)
        logFile = 'emulator' + str(currentId) + '.log'
        command = 'java -Xms2000m -Xmx4000m -jar ' + jarFile + ' ' + configFile + ' ' + str(currentId) + ' ' + str(usersPerProxy) + ' ' + useCustomJDBC + ' ' + str(config.TPCC_TEST_TIME) + ' > ' + logFile + ' &'
        if config.IS_LOCALHOST:
            command = 'java -jar tpcc-client.jar ' + jarFile + ' ' + configFile + ' ' + str(currentId) + ' ' + str(usersPerProxy) + ' ' + useCustomJDBC + ' ' + str(config.TPCC_TEST_TIME) + ' > ' + logFile + ' &'
        
        logger.info('starting emulator %s with %s users', currentId, usersPerProxy)
        logger.info('%s',command)
        with cd(config.DEPLOY_DIR):
            run(command)  

################################################################################################
#   SETUP METHODS
################################################################################################

def distributeCode():
    run('mkdir -p ' + config.DEPLOY_DIR)
    with cd(config.BASE_DIR), hide('output','running'), settings(warn_only=True):
        run('rm -rf ' + config.DEPLOY_DIR + '/*')                
        put(config.JARS_DIR + '/*.jar', config.DEPLOY_DIR)
        put(config.PROJECT_DIR + '/resources/configs', config.DEPLOY_DIR)
        put(config.PROJECT_DIR + '/experiments', config.DEPLOY_DIR)
        put(config.PROJECT_DIR + '/resources/*.sql', config.DEPLOY_DIR)
        put(config.PROJECT_DIR + '/resources/*.properties', config.DEPLOY_DIR)

def downloadLogsTo(outputDir):
    logger.info('%s is moving log files to proper directory', env.host_string)
    
    with cd(config.DEPLOY_DIR), hide('warnings', 'output', 'running'), settings(warn_only=True):
        run('cp *.temp ' + outputDir)
        run('cp *.log '  + outputDir + "/logs")

@parallel
def prepareTPCCDatabase():

    mysqlPackage = ''
    if config.JDBC == 'crdt':
        mysqlPackage = 'mysql-5.6_ready.tar.gz'
    elif config.JDBC == 'galera':
        mysqlPackage = 'mysql-5.6-galera_ready.tar.gz'
    else:
        logger.error("unexpected driver: %s", config.JDBC)
        sys.exit()
    
    logger.info('unpacking database at: %s', env.host_string)
    with cd(config.BASE_DIR), hide('output','running'):
        run('rm -rf mysql*')
        run('cp ' + config.BACKUPS_DIR + '/' + mysqlPackage + " " + config.BASE_DIR)
        run('tar zxvf ' + mysqlPackage)

    time.sleep(3)

################################################################################################
#   HELPER METHODS
################################################################################################

def stopJava():
    command = 'ps ax | grep java'
    with settings(warn_only=True):
        output = run(command)
    for line in output.splitlines():                    
        if 'java' in line:        
            pid = int(line.split(None, 1)[0])            
            with settings(warn_only=True):
                run('kill -9 ' + str(pid))                
    
def stopMySQL():
    with settings(warn_only=True),hide('output'), cd(config.MYSQL_DIR):
        run(config.MYSQL_SHUTDOWN_COMMAND)
    with settings(warn_only=True),hide('output'), cd(config.GALERA_MYSQL_DIR):
        run(config.MYSQL_SHUTDOWN_COMMAND)        

def isPortOpen(port):
    with settings(warn_only=True),hide('output'):
        output = run('netstat -tan | grep ' + port)
        return output.find('LISTEN') != -1

def areClientsRunning(emulatorsNumber):
    for y in xrange(1, emulatorsNumber+1):
        currentId = str(y)
        logFile = 'emulator' + str(currentId) + '.log'
        with cd(config.DEPLOY_DIR):
            output = run('tail ' + logFile)
            if 'CLIENT TERMINATED' not in output:
                logger.warn('emulator %s not yet finished', currentId)
                return "True"
    return "False"

def executeTerminalCommand(command):
    local(command)    

def executeTerminalCommandAtDir(command, atDir):
    with lcd(atDir):
        return executeTerminalCommand(command)

def executeRemoteTerminalCommandAtDir(hostName, command, atDir):
    with cd(atDir):
        return executeRemoteTerminalCommand(hostName, command)

def executeRemoteTerminalCommand(hostName, command):
    hostList = [hostName]
    return execute(executeRemoteCommand, command, hosts=hostList)

def killRunningProcesses():
    logger.info('cleaning running processes')    
    with hide('running','output','warnings'):
        execute(stopJava, hosts=config.distinct_nodes)
        time.sleep(1)
        execute(stopMySQL, hosts=config.database_nodes)
        time.sleep(1)
        execute(stopJava, hosts=config.distinct_nodes)
        time.sleep(1)        

def cleanOutputFiles():
    with cd(config.BASE_DIR), hide('output','running'), settings(warn_only=True):
        run('rm -rf ' + config.DEPLOY_DIR + '/*.log')
        run('rm -rf ' + config.DEPLOY_DIR + '/*.temp')

def executeRemoteCommand(command):
    output = run(command)
    return output

def generateClusterAddress():
    clusterAddress = "gcomm://"

    for i in range(len(config.database_nodes)):
        nodeName = config.database_nodes[i]
        clusterAddress += nodeName

        if i < len(config.database_nodes) - 1:
            clusterAddress += ","

    return clusterAddress




