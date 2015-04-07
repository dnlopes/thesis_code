from fabric.api import env, local, lcd, roles, parallel, cd, put, execute, settings, abort, hide, task, sudo, run
import time
import sys
import xml.etree.ElementTree as ET

# TPCW Deployment Script
# Author: David Lopes
# Nova university of lisbon
# Last update: nov 2013 
#------------------------------------------------------------------------------
#environment variables
# REMEMBER TO UPDATE THE FILE ENVIROHMENT.SH AS WELL
#------------------------------------------------------------------------------

MYSQL_SHUTDOWN_COMMAND='bin/mysqladmin -u sa --password=101010 --socket=/tmp/mysql.sock shutdown'
MYSQL_START_COMMAND='bin/mysqld_safe --no-defaults'

TOMCAT_START='bin/startup.sh'
TOMCAT_SHUTDOWN_COMMAND='bin/shutdown.sh'

env.user = 'dp.lopes'
env.shell = "/bin/bash -l -i -c" 

BASE_DIR = '/local/' + env.user
HOME_DIR = '/home/' + env.user

MYSQL_DIR = BASE_DIR + '/mysql-5.6'
MYSQL_DIR = BASE_DIR + '/tomcat6'


database_nodes = ["node1", "node2"]
app_server_nodes = []

env_vars = dict()
env.roledefs = {
    'configuratorNode': ["node1"],
    'databases': database_nodes
}

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
@roles('configuratorNode')
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
def startDatabases():
    mysql_start()
    time.sleep(1)
    if not is_mysql_running():
        return False
    else:
        return True     
    
@task
def runTPCWExperiment(configFile):
    print configFile
    parseConfigFile(configFile)
    pass
    # load configFile
    # prepareTPCW experiment
    # compile code
    # put jars and files in place
    # stop/start databases.
    # start coordinator
    # start replicators
    # start emulators

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


def parseConfigFile(configFile):
    e = ET.parse(configFile).getroot()
    for replicator in e.iter('replicator'):
        host = replicator.get('host')
        port = replicator.get('port')
        print host, port



