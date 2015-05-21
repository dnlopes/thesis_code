from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run
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


#NUMBER_USERS=[1]
#NUMBER_REPLICAS=[1]
#JDCBs=['mysql_crdt']
NUMBER_USERS=[1,3,5,15,30,45,60]
NUMBER_REPLICAS=[3,5]
JDCBs=['mysql_crdt']
#JDCBs=['mysql_jdbc', 'mysql_crdt']

logger = logging.getLogger('simple_example')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

env.shell = "/bin/bash -l -i -c" 
#env.user = 'dnl'
env.user = 'dp.lopes'

CONFIG_FILE=''
LOG_FILE_DIR=''

TPCC_TEST_TIME=10

MYSQL_SHUTDOWN_COMMAND='bin/mysqladmin -u sa --password=101010 --socket=/tmp/mysql.sock shutdown'
MYSQL_START_COMMAND='bin/mysqld_safe --no-defaults'
TOMCAT_START='bin/startup.sh'
TOMCAT_SHUTDOWN_COMMAND='bin/shutdown.sh'


BASE_DIR = '/local/' + env.user
DEPLOY_DIR = BASE_DIR + '/deploy'
MYSQL_DIR = BASE_DIR + '/mysql-5.6'

HOME_DIR = '/home/' + env.user
LOGS_DIR = HOME_DIR + '/logs'
BACKUPS_DIR = HOME_DIR + '/backups'
PROJECT_DIR = HOME_DIR + '/code'
JARS_DIR = PROJECT_DIR + '/dist/jars'

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

def runOriginExperiment():
    pass

def runWeakDBExperiment():
    pass    

@task
def benchmarkTPCC(configsFilesBaseDir):
    customJDBC=''
    weakDBExperiment = False
    for replicasNum in NUMBER_REPLICAS:
        global CONFIG_FILE
        CONFIG_FILE = configsFilesBaseDir + '/'
        #CONFIG_FILE += 'tpcc_localhost_' + str(replicasNum) + 'node.xml'
        CONFIG_FILE += 'tpcc_cluster_' + str(replicasNum) + 'node.xml'
        logger.info('starting tests with %d replicas', replicasNum)
        parseConfigFile()
        with hide('running','output'):
            execute(endExperiment, hosts=distinct_nodes)
        
        prepareCode()        
        for jdbc in JDCBs:
            if jdbc == 'mysql_crdt':
                weakDBExperiment = True
                customJDBC='true'
            else:
                customJDBC='false'     
                weakDBExperiment = False               
            for usersNum in NUMBER_USERS:                
                usersPerReplica = usersNum / replicasNum
                global LOG_FILE_DIR
                LOG_FILE_DIR = time.strftime("%H_%M_%S") + '_test_'                
                if weakDBExperiment:
                    LOG_FILE_DIR += 'crdt_'
                else:
                    LOG_FILE_DIR += 'orig_'
                LOG_FILE_DIR += str(replicasNum) + 'replicas_'
                LOG_FILE_DIR += str(usersNum) + 'users/'                
                logger.info('this experiment will be logged to ' + LOG_FILE_DIR) 
                
                # preparar database
                logger.info('preparing tpcc database')
                with hide('running','output'):
                    execute(prepareTPCCDatabase, hosts=database_nodes)  

                #start databases
                with hide('running','output'):
                    dbResults = execute(startDatabases, hosts=database_nodes)
                    for key, value in dbResults.iteritems():
                        if value == '0':
                            logger.error('database at %s failed to start', key)
                            sys.exit()
                logger.info('all databases instances are online') 

                #start coordinators
                with hide('running','output'):
                    coordResults = execute(startCoordinators, hosts=coordinators_nodes)
                    for key, value in coordResults.iteritems():
                        if value == '0':
                            logger.error('coordinator at %s failed to start', key)
                            sys.exit()
                logger.info('all coordinators are online')                           

                #start replicators
                with hide('running','output'):
                    replicatorResults = execute(startReplicators, hosts=replicators_nodes)
                    for key, value in replicatorResults.iteritems():
                        if value == '0':
                            logger.error('replicator at %s failed to start', key)
                            sys.exit()
                logger.info('all replicators are online') 

                #start clients
                with hide('running','output'):
                    execute(startTPCCclients, usersPerReplica, customJDBC, hosts=proxies_nodes)
                
                logger.info('the experiment is running') 
                time.sleep(15)
                time.sleep(TPCC_TEST_TIME)   
                #isRunning = True
                #while isRunning:
                #    time.sleep(2)    
                #    logger.info('check experiment status')   
                #    with hide('output','running'):
                #        stillRunning = execute(checkClientsIsRunning, hosts=proxies_nodes)
                #    for key, value in stillRunning.iteritems():
                #        if value == '1':
                #            isRunning = True
                #            logger.info('experiment still running')                
                #            break
                #        else:
                #            isRunning = False
                #    if isRunning:
                #        time.sleep(5)
                #    else:
                #        logger.info('experiment has finished!')   
                #        break                        
                logger.info('experiment has finished!')
                with hide('running','output'):
                    execute(endExperiment, hosts=distinct_nodes)
                    execute(pullLogs, hosts=distinct_nodes)
                logger.info('this experiment has ended. moving to the next iteration')
                
def prepareTPCW():
    if not is_mysql_running():
        mysql_start()

    time.sleep(1)
    if not is_mysql_running():
        print('mysql is not running. Exiting')
        sys.exit()

    export_file = HOME_DIR + '/backups/tpcw_export.sql'
    exportDatabase('tpcw', export_file)

@parallel
def startDatabases():
    command = 'nohup ' + MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &'  
    logger.info('starting database: %s',command)
    with cd(MYSQL_DIR), hide('running','output'):    
        run(command)    
    time.sleep(10)
    if not isPortOpen('3306'):
        return '0'
    return '1'

@parallel
def startCoordinators():
    currentId = coordinators_map.get(env.host_string)    
    port = coordinatorsHostToPortMap.get(env.host_string)
    logFile = 'coordinator_' + str(currentId) + ".log"
    command = 'java -jar coordinator.jar ' + CONFIG_FILE + ' ' + currentId + ' > ' + logFile + ' &'
    logger.info('starting coordinator at %s', env.host_string)
    logger.info('%s',command)
    with cd(DEPLOY_DIR), hide('running','output'):
        run(command)
    time.sleep(8)
    if not isPortOpen(port):
        return '0'
    return '1'

@parallel
def startReplicators():
    currentId = replicators_map.get(env.host_string)    
    port = replicatorsHostToPortMap.get(env.host_string)
    logFile = 'replicator_' + str(currentId) + ".log"
    command = 'java -jar replicator.jar ' + CONFIG_FILE + ' ' + currentId + ' > ' + logFile + ' &'
    logger.info('starting replicator at %s', env.host_string)
    logger.info('%s',command)
    with cd(DEPLOY_DIR), hide('running','output'):
        run(command)
    time.sleep(8)
    if not isPortOpen(port):
        return '0'
    return '1'

@parallel
def startTPCCclients(clientsNum, useCustomJDBC):
    currentId = proxies_map.get(env.host_string)    
    port = proxiesHostToPortMap.get(env.host_string)
    logFile = 'proxy_' + str(currentId) + ".log"
    command = 'java -jar tpcc-client.jar ' + CONFIG_FILE + ' ' + currentId + ' ' + str(clientsNum) + ' ' + useCustomJDBC + ' ' + str(TPCC_TEST_TIME) + ' > ' + logFile + ' &'
    logger.info('starting client at %s', env.host_string)
    logger.info('%s',command)
    with cd(DEPLOY_DIR):
        run(command)
  
@parallel
def endExperiment():
    logger.info('cleaning running processes after experiment has finished')
    with hide('output','running','warnings'):
        stopJava()
        stopMySQL()
    
    logger.info('done!')
    time.sleep(5)

def pullLogs():
    logger.info('downloading log files from nodes')
    filesToDownload = DEPLOY_DIR + '/*.out'
    filesToDownload2 = DEPLOY_DIR + '/*.log'

    with lcd(LOGS_DIR), hide('warnings'), settings(warn_only=True):
        local('mkdir -p ' + LOG_FILE_DIR)
        get(filesToDownload, LOG_FILE_DIR)
        get(filesToDownload2, LOG_FILE_DIR)   
    
    logger.info('done!')

def killProcesses(configFile):
    loadInputFile(configFile)    
    parseConfigFile()
    execute(stopMySQL, hosts=database_nodes)
    execute(stopJava, hosts=coordinators_nodes)
    execute(stopJava, hosts=replicators_nodes)
    execute(stopJava, hosts=proxies_nodes)

def prepareCode():
    logger.info('compiling source code')
    with lcd(PROJECT_DIR), hide('output','running'):
        local('ant purge tpcc-dist')
    logger.info('uploading distribution to nodes: %s', distinct_nodes)
    logger.info('deploying jars, resources and config files')
    with hide('output','running'):
        execute(distributeCode, hosts=distinct_nodes)

def distributeCode():
    run('mkdir -p ' + DEPLOY_DIR)
    with cd(BASE_DIR), hide('output','running'), settings(warn_only=True):
        run('rm ' + DEPLOY_DIR + '/*.jar')
        run('rm ' + DEPLOY_DIR + '/*.sql')
        run('rm ' + DEPLOY_DIR + '/*.properties')
        run('rm -rf ' + DEPLOY_DIR + '/configs')                
        put(JARS_DIR + '/*.jar', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/configs', DEPLOY_DIR)
        put(PROJECT_DIR + '/experiments', DEPLOY_DIR)
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
            os.kill(pid, signal.SIGKILL)            
    
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
        #dbsSet.add(dbHost)
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

    logger.info('Databases: %s', database_nodes)
    logger.info('Proxies: %s', proxies_nodes)
    logger.info('Replicators: %s', replicators_nodes)
    logger.info('Coordinators: %s', coordinators_nodes)
    logger.info('Distinct nodes: %s', distinct_nodes)

@parallel
def prepareTPCCDatabase():
    # assume mysql is not running
    logger.info('unpacking database at: %s', env.host_string)
    with cd(BASE_DIR), hide('output','running'):
        run('rm -rf mysql*')
        run('cp ' + BACKUPS_DIR + '/mysql-5.6_ready.tar.gz ' + BASE_DIR)
        run('tar zxvf mysql-5.6_ready.tar.gz')
    time.sleep(3)

@parallel
def checkClientsIsRunning():
    proc1 = subprocess.Popen(['ps', 'ax'], stdout=subprocess.PIPE)
    proc2 = subprocess.Popen(shlex.split('grep ' + env.user),stdin=proc1.stdout,
                         stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    proc1.stdout.close()
    out,err=proc2.communicate()            
    for line in out.splitlines():                    
        print line
        if 'java' in line:
            if "tpcc-client" in line: 
                return '1'
            else:
                return '0'

