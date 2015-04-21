from fabric.api import env, local, lcd, roles, parallel, cd, put, execute, settings, abort, hide, task, sudo, run
import time
import sys
import xml.etree.ElementTree as ET
from sets import Set
import logging

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
MYSQL_DIR = BASE_DIR + '/tomcat6'


distinct_nodes = []
database_nodes = []
replicators_nodes = []
coordinators_nodes = []
proxies_nodes = []

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

@task
@roles('configuratorNode')
def prepareTPCW():
    if not is_mysql_running():
        mysql_start()

    time.sleep(1)
    if not is_mysql_running():
        print('mysql is not running. Exiting')
        sys.exit()

    export_file = HOME_DIR + '/backups/tpcw_export.sql'
    exportDatabase('tpcw', export_file)

@task
def prepareTPCC():
    if not is_mysql_running():
        mysql_start()

    time.sleep(1)
    if not is_mysql_running():
        logger.error('mysql is not running. Exiting')
        sys.exit()

    export_file = HOME_DIR + '/backups/tpcc_export.sql'
    exportDatabase('tpcc', export_file)

@task
def startCoordinators():
    currentId = coordinators_map.get(env.host_string)    
    port = coordinatorsHostToPortMap.get(env.host_string)

    logFile = 'coordinator_' + currentId + ".log"
    logger.info('starting coordinator at %s with id %s', env.host_string, currentId)
    command = 'java -jar coordinator.jar ' + CONFIG_FILE + ' ' + currentId + ' > logs/' + logFile + ' &'
    with cd(DEPLOY_DIR):
        run(command)
    time.sleep(5)
    if not isPortOpen(port):
        return '0'
    return '1'

@task
def startReplicators():
    currentId = replicators_map.get(env.host_string)    
    port = replicatorsHostToPortMap.get(env.host_string)

    logFile = 'replicator_' + currentId + ".log"
    logger.info('starting replicator at %s with id %s', env.host_string, currentId)
    command = 'java -jar replicator.jar ' + CONFIG_FILE + ' ' + currentId + ' > logs/' + logFile + ' &'
    with cd(DEPLOY_DIR):
        run(command)
    time.sleep(5)
    if not isPortOpen(port):
        return '0'
    return '1'

@task
def setupExperiment(configFile):
    print
    global CONFIG_FILE
    CONFIG_FILE=configFile
    parseConfigFile()
    prepareCode()

@task
def prepareCode():
    logger.info('compiling source code')
    with lcd(PROJECT_DIR):
        local('ant purge compile')
    logger.info('uploading distribution to nodes: %s', distinct_nodes)
    logger.info('deploying jars, resources and config files')
    with hide('output'):
        execute(distributeCode, hosts=distinct_nodes)

@task
def startDatabases():
    mysql_start()
    time.sleep(1)
    if not is_mysql_running():
        return False
    else:
        return True     

@task
@parallel
def distributeCode():
    with cd(BASE_DIR):
        run('rm -rf ' + DEPLOY_DIR + ' ; mkdir -p ' + DEPLOY_DIR + '/logs')
        put(JARS_DIR + '/*.jar', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/configs', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/*.sql', DEPLOY_DIR)
        put(PROJECT_DIR + '/resources/*.properties', DEPLOY_DIR)

@task
def runTPCCExperiment(configFile):
    global CONFIG_FILE
    CONFIG_FILE = configFile
    parseConfigFile()
    coordResults = execute(startCoordinators, hosts=replicators_nodes)
    for key, value in coordResults.iteritems():
        if value == '0':
            logger.error('coordinator at %s failed to start', key)
            sys.exit()

    replicatorResults = execute(startReplicators, hosts=replicators_nodes)
    for key, value in replicatorResults.iteritems():
        if value == '0':
            logger.error('replicator at %s failed to start', key)
            sys.exit()
    
@task
def exportDatabase(databaseName, outputFile):
    with cd(MYSQL_DIR):
        run('bin/mysqldump -u sa --password=101010 ' + databaseName + ' --socket=/tmp/mysql.sock > ' + outputFile)
        
@task    
def mysql_start():
    with cd(MYSQL_DIR):    
        run('nohup ' + MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &')    

@task        
def mysql_stop():
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
    
