from fabric.api import env, local, lcd, roles, parallel, cd, put, execute, settings, abort, hide, task, sudo, run
import time
import sys
import xml.etree.ElementTree as ET
from sets import Set
import logging
import shlex
import subprocess, signal
import os
from parseConfigFile import parseConfigInput

#------------------------------------------------------------------------------
# Deployment Scripts
# Author: David Lopes
# Nova university of lisbon
# Last update: April, 2015
#------------------------------------------------------------------------------

logger = logging.getLogger('simple_example')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

CONFIG_FILE=''
LOG_FILE_DIR=''

MYSQL_SHUTDOWN_COMMAND='bin/mysqladmin -u sa --password=101010 --socket=/tmp/mysql.sock shutdown'
MYSQL_START_COMMAND='bin/mysqld_safe --no-defaults'

TOMCAT_START='bin/startup.sh'
TOMCAT_SHUTDOWN_COMMAND='bin/shutdown.sh'

env.user = 'dp.lopes'
env.shell = "/bin/bash -l -i -c" 

BASE_DIR = '/local/' + env.user 
PROJECT_DIR = '/home/' + env.user + '/code'
JARS_DIR = PROJECT_DIR + '/dist/jars'
DEPLOY_DIR = BASE_DIR + '/deploy'

MYSQL_DIR = BASE_DIR + '/mysql-5.6'

distinct_nodes = []
database_nodes = []
replicators_nodes = []
coordinators_nodes = []
proxies_nodes = []

configsMap = dict()
database_map = dict()
replicators_map = dict()
coordinators_map = dict()
proxies_map = dict()

replicatorsHostToPortMap = dict()
coordinatorsHostToPortMap = dict()
proxiesHostToPortMap = dict()

env_vars = dict()
env.roledefs = {
    'configuratorNode': ["localhost"],
    'databases': database_nodes, 
    'replicators': replicators_nodes, 
    'coordinators': coordinators_nodes
}

env.hosts = ['localhost']

def prepareTPCW():
    if not is_mysql_running():
        mysql_start()

    time.sleep(1)
    if not is_mysql_running():
        print('mysql is not running. Exiting')
        sys.exit()

    export_file = HOME_DIR + '/backups/tpcw_export.sql'
    exportDatabase('tpcw', export_file)

def prepareTPCC():
    if not is_mysql_running():
        mysql_start()

    time.sleep(1)
    if not is_mysql_running():
        logger.error('mysql is not running. Exiting')
        sys.exit()

    export_file = HOME_DIR + '/backups/tpcc_export.sql'
    exportDatabase('tpcc', export_file)

def startDatabases():
    mysql_start()
    time.sleep(2)
    if not isPortOpen('3306'):
        return '0'
    return '1'

@parallel
def startCoordinators():
    with cd(DEPLOY_DIR):
        run('mkdir -p logs/' + LOG_FILE_DIR)

    currentId = coordinators_map.get(env.host_string)    
    port = coordinatorsHostToPortMap.get(env.host_string)

    logFile = 'coordinator_' + str(currentId) + ".log"
    logger.info('starting coordinator at %s with id %s', env.host_string, currentId)
    command = 'java -jar coordinator.jar ' + CONFIG_FILE + ' ' + currentId + ' > logs/' + LOG_FILE_DIR + '/' + logFile + ' &'
    with cd(DEPLOY_DIR):
        run(command)
    time.sleep(5)
    if not isPortOpen(port):
        return '0'
    return '1'

@parallel
def startReplicators():
    with cd(DEPLOY_DIR):
        run('mkdir -p logs/' + LOG_FILE_DIR)

    currentId = replicators_map.get(env.host_string)    
    port = replicatorsHostToPortMap.get(env.host_string)
    logFile = 'replicator_' + str(currentId) + ".log"
    logger.info('starting replicator at %s with id %s', env.host_string, currentId)
    command = 'java -jar replicator.jar ' + CONFIG_FILE + ' ' + currentId + ' > logs/' + LOG_FILE_DIR + '/' + logFile + ' &'
    with cd(DEPLOY_DIR):
        run(command)
    time.sleep(5)
    if not isPortOpen(port):
        return '0'
    return '1'

@parallel
def startTPCCClients():
    with cd(DEPLOY_DIR):
        run('mkdir -p logs/' + LOG_FILE_DIR)

    currentId = proxies_map.get(env.host_string)    
    port = proxiesHostToPortMap.get(env.host_string)

    logFile = 'proxy_' + str(currentId) + ".log"
    logger.info('starting proxy at %s with id %s', env.host_string, currentId)
    command = 'java -jar tpcc-client.jar ' + CONFIG_FILE + ' ' + currentId + ' ' + configsMap['users'] + ' > logs/' + LOG_FILE_DIR + '/' + logFile + ' &'
    with cd(DEPLOY_DIR):
        run(command)
    time.sleep(5)
    if not isPortOpen(port):
        return '0'
    return '1'

@task
def setupExperiment(configFile):
    loadInputFile(configFile)
    global CONFIG_FILE
    CONFIG_FILE = configsMap['config_file']
    parseConfigFile()
    prepareCode()

@task
def runTPCCExperiment(configFile):
    loadInputFile(configFile)
    global CONFIG_FILE, LOG_FILE_DIR
    CONFIG_FILE = configsMap['config_file']
    parseConfigFile()
    splittedConfifFile = CONFIG_FILE.split("/")
    LOG_FILE_DIR = time.strftime("%H_%M_%S") + "_" + splittedConfifFile[-1]
    logger.info('this experiment will be logged to logs/' + LOG_FILE_DIR)

    dbResults = execute(startDatabases, hosts=database_nodes)
    for key, value in dbResults.iteritems():
        if value == '0':
            logger.error('database at %s failed to start', key)
            sys.exit()

    logger.info('all databases instances are online')

    coordResults = execute(startCoordinators, hosts=coordinators_nodes)
    for key, value in coordResults.iteritems():
        if value == '0':
            logger.error('coordinator at %s failed to start', key)
            sys.exit()

    logger.info('all coordinators are online')
    replicatorResults = execute(startReplicators, hosts=replicators_nodes)
    for key, value in replicatorResults.iteritems():
        if value == '0':
            logger.error('replicator at %s failed to start', key)
            sys.exit()

    logger.info('all replicators are online')

    proxiesResults = execute(startTPCCClients, hosts=proxies_nodes)
    #for key, value in proxiesResults.iteritems():
    #    if value == '0':
    #        logger.error('proxy at %s failed to start', key)
    #        sys.exit()

    logger.info('all proxies are online')
    logger.info('the experiment is running')    

@task
def killProcesses(configFile):
    global CONFIG_FILE
    CONFIG_FILE=configFile
    parseConfigFile()
    execute(stopMySQL, hosts=database_nodes)
    execute(stopJava, hosts=coordinators_nodes)
    execute(stopJava, hosts=replicators_nodes)
    execute(stopJava, hosts=proxies_nodes)

def prepareCode():
    logger.info('compiling source code')
    with lcd(PROJECT_DIR):
        local('ant purge tpcc-dist')
    logger.info('uploading distribution to nodes: %s', distinct_nodes)
    logger.info('deploying jars, resources and config files')
    with hide('output'):
        execute(distributeCode, hosts=distinct_nodes)

@parallel
def distributeCode():
    with cd(BASE_DIR):
        run('rm -rf ' + DEPLOY_DIR + ' ; mkdir -p ' + DEPLOY_DIR + '/logs')
        put(JARS_DIR + '/*.jar', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/configs', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/*.sql', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/*.properties', DEPLOY_DIR)

def stopJava():
    proc1 = subprocess.Popen(['ps', 'ax'], stdout=subprocess.PIPE)
    proc2 = subprocess.Popen(shlex.split('grep ' + env.user),stdin=proc1.stdout,
                         stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    # Allow proc1 to receive a SIGPIPE if proc2 exits.
    proc1.stdout.close()
    out,err=proc2.communicate()            
    for line in out.splitlines():                    
        if 'java' in line:        
            pid = int(line.split(None, 1)[0])
            print 'killing ' + str(pid)
            os.kill(pid, signal.SIGKILL)            
    
def exportDatabase(databaseName, outputFile):
    with cd(MYSQL_DIR):
        run('bin/mysqldump -u sa --password=101010 ' + databaseName + ' --socket=/tmp/mysql.sock > ' + outputFile)
         
def mysql_start():
    with cd(MYSQL_DIR):    
        run('nohup ' + MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &')    
     
def stopMySQL():
    with settings(warn_only=True),hide('output'), cd(MYSQL_DIR):
        run(MYSQL_SHUTDOWN_COMMAND)

def is_mysql_running():
    with settings(warn_only=True),hide('output'):
        output = run('netstat -tan | grep 3306')
        return output.find('LISTEN') != -1

def isPortOpen(port):
    with settings(warn_only=True),hide('output'):
        output = run('netstat -tan | grep ' + port)
        return output.find('LISTEN') != -1

def parseConfigFile():
    logger.info('parsing config file: %s', CONFIG_FILE)
    #print ">> parsing config file: " + CONFIG_FILE
    e = ET.parse(CONFIG_FILE).getroot()
    distinctNodesSet = Set()
    dbsSet = Set()
    proxiesSet = Set()
    coordinatorsSet = Set()
    replicatorsSet = Set()
    global coordinators_map, replicatorsHostToPortMap, proxiesHostToPortMap, coordinatorsHostToPortMap

    for replicator in e.iter('replicator'):
        replicatorId = replicator.get('id')
        host = replicator.get('host')
        dbHost = replicator.get('dbHost')
        port = replicator.get('port')
        dbsSet.add(dbHost)
        replicatorsSet.add(host)
        distinctNodesSet.add(host)
        replicatorsHostToPortMap[host] = port
        replicators_map[host] = replicatorId                 
    for proxy in e.iter('proxy'):
        proxyId = proxy.get('id')
        host = proxy.get('host')
        dbHost = proxy.get('dbHost')
        port = proxy.get('port')
        dbsSet.add(dbHost)
        proxiesSet.add(host) 
        distinctNodesSet.add(host)
        proxies_map[host] = proxyId     
        proxiesHostToPortMap[host] = port    
    for coordinator in e.iter('coordinator'):
        coordinatorId = coordinator.get('id')
        port = coordinator.get('port')
        host = coordinator.get('host')
        dbHost = coordinator.get('dbHost')
        dbsSet.add(dbHost)
        coordinatorsSet.add(host)      
        distinctNodesSet.add(host) 
        coordinators_map[host] = coordinatorId   
        coordinatorsHostToPortMap[host] = port      

    global database_nodes
    global replicators_nodes
    global proxies_nodes
    global distinct_nodes
    global coordinators_nodes

    database_nodes = list(dbsSet)
    proxies_nodes = list(proxiesSet)
    replicators_nodes = list(replicatorsSet)
    coordinators_nodes = list(coordinatorsSet)
    distinct_nodes = list(distinctNodesSet)

    logger.debug('Databases: %s', database_nodes)
    logger.debug('Proxies: %s', proxies_nodes)
    logger.debug('Replicators: %s', replicators_nodes)
    logger.debug('Coordinators: %s', coordinators_nodes)
    logger.debug('Distinct nodes: %s', distinct_nodes)

def loadInputFile(configFile):
    global configsMap
    configsMap = parseConfigInput(configFile)     
    

