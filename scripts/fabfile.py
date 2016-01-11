from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run, \
	warn_only
import time
import sys
import logging
import utils as utils
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

	#http://galeracluster.com/documentation-webpages/mysqlwsrepoptions.html#wsrep-sync-wait
	clusterAddress = utils.generateClusterAddress()

	mysqlCommand = config.MYSQL_START_COMMAND + ' --wsrep-sync-wait=3 --wsrep_cluster_address="' + clusterAddress + '"'
	if isMaster:
		mysqlCommand += " --wsrep-new-cluster"

	command = 'nohup ' + mysqlCommand + ' >& /dev/null < /dev/null &'

	logger.info('starting database at %s', env.host_string)
	logger.info(command)
	with cd(config.GALERA_MYSQL_DIR), hide('running', 'output'):
		run(command)

	time.sleep(20)

	if not isPortOpen(config.MYSQL_PORT):
		return '0'
	return '1'

def startClusterDatabases(initialFlag):
	logger.info('starting MySQL cluster')

	mgmt_node = ['node1']
	output = execute(startMgmtNode, hosts=mgmt_node)
	for key, value in output.iteritems():
		if value == '0':
			logger.warn('mgmt_node at %s failed to start', key)
			return '0'

	time.sleep(2)

	# start data nodes
	output = execute(startClusterDataNode, initialFlag, hosts=config.database_nodes)
	for key, value in output.iteritems():
		if value == '0':
			logger.warn('data node at %s failed to start', key)
			return '0'

	time.sleep(2)

	#start mysql instances
	output = execute(startClusterMySQLInstances, hosts=config.database_nodes)
	for key, value in output.iteritems():
		if value == '0':
			logger.warn('mysqld-cluster instance at %s failed to start', key)
			return '0'

	logger.info('MySQL cluster is up and running!')
	return '1'

@parallel
def startMgmtNode():
	numberNodes = len(config.database_nodes)
	configFile = 'config' + str(numberNodes) + '.ini'
	command = config.CLUSTER_START_MGMG_COMMAND + configFile
	logger.info('starting mgmt_node at %s', env.host_string)
	logger.info(command)
	with cd(config.MYSQL_CLUSTER_DIR), hide('running', 'output'):
		run(command)

	time.sleep(5)

	if not isPortOpen(config.MYSQL_CLUSTER_MGMT_NODE_PORT):
		return '0'
	return '1'

@parallel
def startClusterDataNode(initialFlag):
	logger.info('starting data_node at %s', env.host_string)
	command = 'rm -rf cluster-data'
	logger.info(command)
	with cd(config.BASE_DIR), hide('running', 'output'):
		run(command)

	if initialFlag:
		command = 'mkdir -p ' + config.MYSQL_CLUSTER_DATA_DIR
		logger.info(command)
		with cd(config.BASE_DIR), hide('running', 'output'):
			run(command)

		command = config.CLUSTER_START_DATA_NODE_COMMAND + ' --initial'
		logger.info(command)
		with cd(config.MYSQL_CLUSTER_DIR), hide('running', 'output'):
			run(command)
	else:
		command = 'tar zxvf cluster_data.tar.gz'
		logger.info(command)
		with cd(config.BASE_DIR), hide('running', 'output'):
			run(command)

		command = config.CLUSTER_START_DATA_NODE_COMMAND
		logger.info(command)
		with cd(config.MYSQL_CLUSTER_DIR), hide('running', 'output'):
			run(command)

	time.sleep(10)

	if not isPortOpen(config.MYSQL_CLUSTER_DATA_NODE_PORT):
		return '0'
	return '1'

@parallel
def startDatabases():
	command = 'nohup ' + config.MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &'

	logger.info('starting database at %s', env.host_string)
	logger.info(command)
	with cd(config.MYSQL_DIR), hide('running', 'output'):
		run(command)

	time.sleep(25)

	if not isPortOpen(config.MYSQL_PORT):
		return '0'
	return '1'

@parallel
def startClusterMySQLInstances():
	command = 'nohup ' + config.CLUSTER_MYSQL_START_COMMAND + ' >& /dev/null < /dev/null &'
	logger.info('starting database at %s', env.host_string)
	logger.info(command)
	with cd(config.MYSQL_CLUSTER_DIR), hide('running', 'output'):
		run(command)

	time.sleep(25)

	if not isPortOpen(config.MYSQL_PORT):
		return '0'
	return '1'

@parallel
def preloadDatabase(dbName):
	command = 'java -jar preload-tpcc.jar localhost ' + dbName
	logger.info('preloading database at %s', env.host_string)
	logger.info(command)
	with cd(config.DEPLOY_DIR), hide('running', 'output'):
		run(command)


@parallel
def startCoordinators():
	currentId = config.coordinators_map.get(env.host_string)

	logFile = config.FILES_PREFIX + 'coordinator' + str(currentId) + '.log'
	command = 'java ' + config.ZOOKEEPER_LOG4J_FILE + ' -Xms1000m -Xmx2000m -jar zookeeper-server.jar ' + config.ZOOKEEPER_CFG_FILE + ' > ' + logFile + ' &'
	if config.IS_LOCALHOST:
		command = 'java -jar zookeeper-server.jar' + ' > ' + logFile + ' &'

	logger.info('starting zookeeper at %s', env.host_string)
	logger.info('%s', command)

	with cd(config.ZOOKEEPER_DATA_DIR), hide('running', 'output'):
		run('echo ' + str(currentId) + ' > myid')

	with cd(config.DEPLOY_DIR):
		run('echo "" >> zoo.cfg')
		run('echo -e "' + config.ZOOKEEPER_SERVERS_STRING + '" >> zoo.cfg')
		run(command)

	time.sleep(20)
	if not isPortOpen(config.ZOOKEEPER_CLIENT_PORT):
		return '0'
	return '1'


@parallel
def startReplicators(configFile):
	currentId = config.replicators_map.get(env.host_string)
	port = config.replicatorsIdToPortMap.get(currentId)
	logFile = config.FILES_PREFIX + 'replicator' + str(currentId) + '.log'
	jarFile = 'replicator.jar'

	command = 'java ' + config.WEAK_DB_LOG4J_FILE + ' -Xms4000m -Xmx6000m -jar ' + jarFile + ' ' + configFile + ' ' + config.ENVIRONMENT_FILE + ' ' + str(currentId) + ' > ' + logFile + ' &'
	if config.IS_LOCALHOST:
		command = 'java -jar ' + jarFile + ' ' + configFile + ' ' + config.ENVIRONMENT_FILE + ' ' + str(
			currentId) + ' > ' + logFile + ' &'

	logger.info('starting replicator at %s', env.host_string)
	logger.info('%s', command)
	with cd(config.DEPLOY_DIR), hide('running', 'output'):
		run(command)

	time.sleep(20)
	if not isPortOpen(port):
		return '0'
	return '1'


def startTPCCclients(configFile, proxiesNumber, usersPerProxy, useCustomJDBC):
	jarFile = 'tpcc-client.jar'
	jdbc = config.JDBC

	if(jdbc == 'galera'):
		jdbc = 'mysql'

	currentId = config.emulators_map.get(env.host_string)
	#workloadFile = config.TPCC_WORKLOADS_DIR + "/" + config.emulators_workloads.get(currentId)
	workloadFile = config.TPCC_WORKLOAD_FILE
	logFile = config.FILES_PREFIX + 'emulator' + str(currentId) + '.log'
	command = 'java ' + config.WEAK_DB_LOG4J_FILE + ' -Xms4000m -Xmx6000m -jar ' + jarFile + ' ' + configFile + ' ' + config.ENVIRONMENT_FILE + ' ' + workloadFile + \
			  ' ' + str(currentId) + ' ' + str(usersPerProxy) + ' ' + str(config.TPCC_TEST_TIME) + ' ' + jdbc + ' > ' + logFile + ' &'
	if config.IS_LOCALHOST:
		command = 'java -jar ' + jarFile + ' ' + configFile + ' ' + config.ENVIRONMENT_FILE + ' ' + workloadFile + \
				  ' ' + str(currentId) + ' ' + str(usersPerProxy) + ' ' + str(config.TPCC_TEST_TIME) + ' ' + jdbc + ' > ' + logFile + ' &'

	logger.info('starting emulator %s with %s users', currentId, usersPerProxy)
	logger.info('%s', command)
	with cd(config.DEPLOY_DIR):
		run(command)


################################################################################################
#   SETUP METHODS
################################################################################################

def distributeCode():
	with cd(config.BASE_DIR), hide('output', 'running'), settings(warn_only=True):
		run('rm -rf ' + config.DEPLOY_DIR + '/*')
		run('mkdir -p ' + config.DEPLOY_DIR + '/src')
		put(config.JARS_DIR + '/*.jar', config.DEPLOY_DIR)
		put(config.PROJECT_DIR + '/resources/*', config.DEPLOY_DIR)
		put(config.PROJECT_DIR + '/src/*', config.DEPLOY_DIR + '/src')

def populateTpccDatabase():
	logger.info('populating database from %s', env.host_string)

	command = 'bin/mysql --defaults-file=my.cnf -u sa -p101010 -e \"source /home/dp.lopes/code/scripts/sql/create_tpcc_ndb.sql\"'
	logger.info(command)
	with cd(config.DEPLOY_DIR), hide('running', 'output'):
		run(command)

	command = 'java -jar tpcc-gendb.jar localhost tpcc 3'
	logger.info(command)
	with cd(config.DEPLOY_DIR), hide('running', 'output'):
		run(command)
		return '1'

@parallel
def downloadLogsTo(outputDir):
	logger.info('downloading log files from %s', env.host_string)
	with cd(config.DEPLOY_DIR), hide('warnings', 'output', 'running'), settings(warn_only=True):
		get(config.DEPLOY_DIR + "/*.csv", outputDir)
		get(config.DEPLOY_DIR + "/*.log", outputDir + "/logs")

@parallel
def compressDataNodeFolder():
	command = 'tar zcvf cluster_data.tar.gz cluster-data/'
	logger.info(command)
	with cd(config.BASE_DIR), hide('running', 'output'):
		run(command)
		return '1'

@parallel
def prepareDatabase():
	if config.JDBC == 'crdt':
		mysqlPackage = 'mysql-5.6_ready.tar.gz'
	elif config.JDBC == 'galera':
		mysqlPackage = 'mysql-5.6-galera_ready.tar.gz'
	elif config.JDBC == 'cluster':
		mysqlPackage = 'mysql-cluster.tar.gz'
	else:
		logger.error("unexpected driver: %s", config.JDBC)
		sys.exit()

	logger.info('unpacking database at: %s', env.host_string)
	with cd(config.BASE_DIR), hide('output', 'running'):
		run('rm -rf mysql-5.6')
		run('rm -rf mysql-5.6-galera')
		run('rm -rf mysql-cluster-7.0')
		run('tar zxvf ' + mysqlPackage)

@parallel
def prepareCoordinatorLayer():
	if config.JDBC != 'crdt':
		logger.error("unexpected driver: %s", config.JDBC)
		sys.exit()

	zookeeperPackage = 'zookeeper_data_ready.tar.gz'
	logger.info('unpacking zookeeper data_dir at: %s', env.host_string)
	with cd(config.BASE_DIR), hide('output', 'running'):
		run('rm -rf zookeeper')
		#run('cp ' + config.BACKUPS_DIR + '/' + zookeeperPackage + " " + config.BASE_DIR)
		run('tar zxvf ' + zookeeperPackage)

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
			pidString = line.split(None, 1)[0]
			if(pidString.isdigit()):
				pid = int(pidString)
				with settings(warn_only=True):
					run('kill -9 ' + str(pid))

@parallel
def stopMySQL():
	with settings(warn_only=True), hide('output'), cd(config.MYSQL_DIR):
		run(config.MYSQL_SHUTDOWN_COMMAND)
	with settings(warn_only=True), hide('output'), cd(config.GALERA_MYSQL_DIR):
		run(config.MYSQL_SHUTDOWN_COMMAND)
	with settings(warn_only=True), hide('output'), cd(config.CLUSTER_MYSQL_DIR):
		run(config.MYSQL_SHUTDOWN_COMMAND)

	if config.JDBC == 'cluster':
		with settings(warn_only=True), cd(config.CLUSTER_MYSQL_DIR):
			run("bin/ndb_mgm -e shutdown")
			time.sleep(10)
			run("bin/ndb_mgm -e shutdown")
			time.sleep(5)


def isPortOpen(port):
	with settings(warn_only=True), hide('output'):
		output = run('netstat -tan | grep ' + port)
		return output.find('LISTEN') != -1


def areClientsRunning(emulatorsNumber):
	currentId = config.emulators_map.get(env.host_string)
	logFile = config.FILES_PREFIX + 'emulator' + str(currentId) + '.log'
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
	logger.info('killing running processes')
	with hide('running', 'output', 'warnings'):
		execute(stopJava, hosts=config.distinct_nodes)
		time.sleep(1)
		execute(stopMySQL, hosts=config.database_nodes)
		time.sleep(1)
		execute(stopJava, hosts=config.distinct_nodes)
		time.sleep(1)


def cleanOutputFiles():
	with cd(config.BASE_DIR), hide('output', 'running'), settings(warn_only=True):
		run('rm -rf ' + config.DEPLOY_DIR + '/*.log')
		run('rm -rf ' + config.DEPLOY_DIR + '/*.temp')
		run('rm -rf ' + config.DEPLOY_DIR + '/*.csv')


def executeRemoteCommand(command):
	output = run(command)
	return output
