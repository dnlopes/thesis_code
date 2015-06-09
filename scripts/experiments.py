#from fabric.api import env, local, roles, execute, settings
from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run, warn_only
import time
import datetime
import sys
import logging
import time
import sys
import logging
import subprocess, signal
import os
import plots
import configParser as config
import fabfile as fab
import utils as utils

logger = logging.getLogger('expLogger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

TO_DOWNLOAD_COMMANDS = []

################################################################################################
#   CURRENT CONFIGURATION (the only variables needed to modify between benchmarks)
################################################################################################		

################################################################################################		
# LATENCY-THROUGHPUT VARIABLES
################################################################################################		
#NUMBER_REPLICAS=[1]
#JDCBs=['mysql_crdt', "default_jdbc"]
#JDCBs=['crdt','galera']
JDCBs=['cluster']
NUMBER_REPLICAS=[3]
NUMBER_USERS_LIST_1REPLICA=[1]
#NUMBER_USERS_LIST_3REPLICA=[3,6,15,30]
#NUMBER_USERS_LIST_3REPLICA=[3,6,15,30,45,60,90,120,150]
#NUMBER_USERS_LIST_3REPLICA=[12,24,48,96,192,300,450,600]
NUMBER_USERS_LIST_3REPLICA=[12,24,48,96,192,300,450,600]
#NUMBER_USERS_LIST_5REPLICA=[5,10,15,30,45,80,120,180,240]
NUMBER_USERS_LIST_5REPLICA=[10,20,40,80,160,320,500,750,1000]
userListToReplicasNumber = dict()
userListToReplicasNumber[1] = NUMBER_USERS_LIST_1REPLICA
userListToReplicasNumber[3] = NUMBER_USERS_LIST_3REPLICA
userListToReplicasNumber[5] = NUMBER_USERS_LIST_5REPLICA

################################################################################################		
# SCALABILITY VARIABLES
################################################################################################		
SCALABILITY_JDCBs=['crdt']
SCALABILITY_USERS_PER_REPLICA=8
SCALABILITY_NUMBER_REPLICAS=[1,2,3,4,5]
#SCALABILITY_NUMBER_REPLICAS=[1,2]

################################################################################################		
# OBERHEAD VARIABLES
################################################################################################		
OVERHEAD_JDCBs=['mysql','crdt']
OVERHEAD_USERS_LIST=[1,5]

################################################################################################		
# MAIN METHODS
################################################################################################		
@task
def runAllExperiments(configsFilesBaseDir):
	runFullScalabilityExperiment(configsFilesBaseDir)
	runFullLatencyThroughputExperiment(configsFilesBaseDir)

	print "\n"
	logger.info("###########################################################################################")
	logger.info("all experiments have finished!")		
	logger.info("here are the scp commands to download all log files:")
	
	for command in TO_DOWNLOAD_COMMANDS:
		print command
		print "\n"

	logger.info("###########################################################################################")
	print "\n"
	logger.info("Goodbye.")

@task
def runFullLatencyThroughputExperiment(configsFilesBaseDir):
	config.ACTIVE_EXPERIMENT = config.prefix_latency_throughput_experiment
	now = datetime.datetime.now()
	ROOT_OUTPUT_DIR = config.LOGS_DIR + "/" + now.strftime("%d-%m_%Hh%Mm%Ss_") + config.prefix_latency_throughput_experiment

	# first cycle, iteration over the number of replicas
	for numberOfReplicas in NUMBER_REPLICAS:		
		USERS_LIST = userListToReplicasNumber.get(numberOfReplicas)	 
		CONFIG_FILE = configsFilesBaseDir +'/amazon_tpcc_cluster_' + str(numberOfReplicas) + 'node.xml'
		if config.IS_LOCALHOST == True:
			CONFIG_FILE = configsFilesBaseDir +'/tpcc_localhost_' + str(numberOfReplicas) + 'node.xml'
		
		REPLICA_OUTPUT_DIR = ROOT_OUTPUT_DIR + "/" + str(numberOfReplicas) + "replica"		
	  
		config.parseConfigFile(CONFIG_FILE)
		fab.killRunningProcesses()
		prepareCode()

		logger.info("starting tests with %d replicas", numberOfReplicas)
		# second cycle, use different jdbc to run experiment
		for jdbc in JDCBs:
			config.JDBC=jdbc		
			# third cycle, use different number of users per run
			for numberOfUsers in USERS_LIST:
				config.TOTAL_USERS = numberOfUsers
				OUTPUT_DIR = REPLICA_OUTPUT_DIR + "/" + str(numberOfUsers) + "user"
				with hide('output','running','warnings'),settings(warn_only=True):
					local("mkdir -p " + OUTPUT_DIR + "/logs")

				TOTAL_USERS = numberOfUsers
				NUMBER_OF_EMULATORS = len(config.emulators_nodes)
				USERS_PER_EMULATOR = TOTAL_USERS / NUMBER_OF_EMULATORS
				runLatencyThroughputExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS)
				logger.info('moving to the next iteration!')
				
			logger.info('generating plot data file for experiment with %s replicas and %s users', numberOfReplicas, USERS_LIST)
			plots.generateLatencyThroughputDataFile(REPLICA_OUTPUT_DIR, USERS_LIST)

		logger.info("generating plot graphic for experience with %s replicas", numberOfReplicas)
		plots.generateLatencyThroughputPlot(REPLICA_OUTPUT_DIR)
	
	if not config.IS_LOCALHOST:
		scpCommand = "scp -r -P 12034 dp.lopes@di110.di.fct.unl.pt:"
		scpCommand += ROOT_OUTPUT_DIR
		scpCommand += " /Users/dnlopes/devel/thesis/code/weakdb/experiments/logs"
		TO_DOWNLOAD_COMMANDS.append(scpCommand)
		print "\n"		
		logger.info("###########################################################################################")
		logger.info("all experiments have finished!")		
		logger.info("use the following command to copy the logs directories:")
		logger.info(scpCommand)
		logger.info("###########################################################################################")
	print "\n"

@task
def runFullScalabilityExperiment(configsFilesBaseDir):
	config.ACTIVE_EXPERIMENT = config.prefix_scalability_experiment	
	now = datetime.datetime.now()
	ROOT_OUTPUT_DIR = config.LOGS_DIR + "/" + now.strftime("%d-%m_%Hh%Mm%Ss_") + config.prefix_scalability_experiment

	for numberOfReplicas in SCALABILITY_NUMBER_REPLICAS:		
		CONFIG_FILE = configsFilesBaseDir +'/tpcc_cluster_' + str(numberOfReplicas) + 'node.xml'
		if config.IS_LOCALHOST == True:
			CONFIG_FILE = configsFilesBaseDir +'/tpcc_localhost_' + str(numberOfReplicas) + 'node.xml'
		
		REPLICA_OUTPUT_DIR = ROOT_OUTPUT_DIR + "/" + str(numberOfReplicas) + "replica"
		
		config.parseConfigFile(CONFIG_FILE)
		fab.killRunningProcesses()
		prepareCode()

		logger.info("starting tests with %d replicas", numberOfReplicas)
		for jdbc in JDCBs:
			OUTPUT_DIR = REPLICA_OUTPUT_DIR
			with hide('output','running','warnings'),settings(warn_only=True):
				local("mkdir -p " + OUTPUT_DIR + "/logs")

			config.JDBC=jdbc			
			NUMBER_OF_EMULATORS = numberOfReplicas
			USERS_PER_EMULATOR = SCALABILITY_USERS_PER_REPLICA
			TOTAL_USERS = USERS_PER_EMULATOR * NUMBER_OF_EMULATORS
			config.TOTAL_USERS = TOTAL_USERS
			
			runScalabilityExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, numberOfReplicas)
			logger.info('moving to the next iteration!')

	logger.info("generating plot graphic for scalability experience with %s replicas", SCALABILITY_NUMBER_REPLICAS)
	plots.generateScalabilityPlot(ROOT_OUTPUT_DIR, SCALABILITY_NUMBER_REPLICAS, SCALABILITY_JDCBs)

	if not config.IS_LOCALHOST:
		scpCommand = "scp -r -P 12034 dp.lopes@di110.di.fct.unl.pt:"
		scpCommand += ROOT_OUTPUT_DIR
		scpCommand += " /Users/dnlopes/devel/thesis/code/weakdb/experiments/logs"
		TO_DOWNLOAD_COMMANDS.append(scpCommand)
		print "\n"		
		logger.info("###########################################################################################")
		logger.info("all experiments have finished!")		
		logger.info("use the following command to copy the logs directories:")
		logger.info(scpCommand)
		logger.info("###########################################################################################")
	print "\n"

@task
def runFullOverheadExperiment(configsFilesBaseDir):
	config.ACTIVE_EXPERIMENT = config.prefix_overhead_experiment
	now = datetime.datetime.now()
	ROOT_OUTPUT_DIR = config.LOGS_DIR + "/" + now.strftime("%d-%m_%Hh%Mm%Ss_") + config.prefix_overhead_experiment
	CONFIG_FILE = configsFilesBaseDir +'/tpcc_cluster_1node.xml'
	
	if config.IS_LOCALHOST == True:
		CONFIG_FILE = configsFilesBaseDir +'/tpcc_localhost_1node.xml'
	
	config.parseConfigFile(CONFIG_FILE)
	fab.killRunningProcesses()
	prepareCode()	
	
	for jdbc in OVERHEAD_JDCBs:	
		logger.info("starting tests for JDBC: %s", jdbc)	
		config.JDBC=jdbc			
		for numUsers in OVERHEAD_USERS_LIST:
			OUTPUT_DIR = ROOT_OUTPUT_DIR + "/" + str(numUsers) + "user"
			with hide('output','running','warnings'),settings(warn_only=True):
				local("mkdir -p " + OUTPUT_DIR + "/logs")

			NUMBER_OF_EMULATORS = 1
			USERS_PER_EMULATOR = numUsers
			TOTAL_USERS = USERS_PER_EMULATOR * NUMBER_OF_EMULATORS
			config.TOTAL_USERS = TOTAL_USERS

			runOverheadExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, jdbc)		
			logger.info('moving to the next iteration!')

	logger.info("generating plot graphic for overhead experience with %s users", OVERHEAD_USERS_LIST)
	plots.generateOverheadPlot(ROOT_OUTPUT_DIR, OVERHEAD_USERS_LIST, OVERHEAD_JDCBs)

	if not config.IS_LOCALHOST:
		scpCommand = "scp -r -P 12034 dp.lopes@di110.di.fct.unl.pt:"
		scpCommand += ROOT_OUTPUT_DIR
		scpCommand += " /Users/dnlopes/devel/thesis/code/weakdb/experiments/logs"
		TO_DOWNLOAD_COMMANDS.append(scpCommand)
		print "\n"		
		logger.info("###########################################################################################")
		logger.info("all experiments have finished!")		
		logger.info("use the following command to copy the logs directories:")
		logger.info(scpCommand)
		logger.info("###########################################################################################")
	print "\n"

################################################################################################		
# LATENCY-THROUGHPUT METHODS
################################################################################################		
def runLatencyThroughputExperiment(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	
	print "\n"
	logger.info("########################################## starting new Latency-Throughput experiment ##########################################")
	logger.info('>> CONFIG FILE: %s', configFile)
	logger.info('>> DATABASES: %s', config.database_nodes)
	logger.info('>> REPLICATORS: %s', config.replicators_nodes)
	logger.info('>> NUMBER OF EMULATORS: %s', numberEmulators)
	logger.info('>> CLIENTS PER EMULATOR: %s', usersPerEmulator)
	logger.info('>> TOTAL USERS: %s', totalUsers)
	logger.info('>> JDBC: %s', config.JDBC)
	logger.info('>> OUTPUT DIR: %s', outputDir)
	logger.info("################################################################################################################################")
	print "\n"

	success = False
	for attempt in range(4):
		if config.JDBC == 'crdt':
			success = runLatencyThroughputExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		elif config.JDBC == 'galera':
			success = runLatencyThroughputExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		elif config.JDBC == 'cluster':
			success = runLatencyThroughputExperimentCluster(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)		

		if success:
			break
		else:
			logger.error("experiment failed. Retrying...")
			fab.killRunningProcesses()
			execute(fab.cleanOutputFiles, hosts=config.distinct_nodes)

	if not success:
		logger.error("failed to execute experiment after 10 retries. Exiting...")					
		sys.exit()

def runLatencyThroughputExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start. Exiting")
		return False
	
	success = startCoordinatorsLayer(configFile)
	if success == True:
		logger.info('all coordinators are online')       		
	else:
		logger.error("coordination layer failed to start. Exiting")
		return False   
	
	success = startReplicationLayer(configFile)
	if success == True:
		logger.info('all replicators are online')       		
	else:
		logger.error("replication layer failed to start. Exiting")
		return False
	
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "true")

	time.sleep(config.TPCC_TEST_TIME+20)
	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')
			else:
				isRunning = False				
		if isRunning == True:
			attempts += 1
			time.sleep(10)
		else:
			break

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeTemporaryCSVfiles(outputDir, totalUsers, numberEmulators)	
	logger.info('logs can be found at %s', outputDir)

	return True

def runLatencyThroughputExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start")
		return False
		
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "false")

	time.sleep(config.TPCC_TEST_TIME+20)
	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')				
			else:
				isRunning = False
		if isRunning == True:
			attempts += 1
			time.sleep(10)
		else:
			break

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeTemporaryCSVfiles(outputDir, totalUsers, numberEmulators)	
	logger.info('logs can be found at %s', outputDir)

	return True

def runLatencyThroughputExperimentCluster(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start")
		return False
		
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "false")

	time.sleep(config.TPCC_TEST_TIME+20)
	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')				
			else:
				isRunning = False
		if isRunning == True:
			attempts += 1
			time.sleep(10)
		else:
			break

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeTemporaryCSVfiles(outputDir, totalUsers, numberEmulators)	
	logger.info('logs can be found at %s', outputDir)

	return True

################################################################################################		
# OVERHEAD METHODS
################################################################################################		
def runOverheadExperiment(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers, jdbc):
	print "\n"
	logger.info("########################################## starting overhead experiment ##########################################")
	logger.info('>> CONFIG FILE: %s', configFile)
	logger.info('>> DATABASES: %s', config.database_nodes)
	if jdbc == 'crdt':
		logger.info('>> REPLICATORS: %s', config.replicators_nodes)
	logger.info('>> NUMBER OF EMULATORS: %s', numberEmulators)
	logger.info('>> CLIENTS PER EMULATOR: %s', usersPerEmulator)
	logger.info('>> TOTAL USERS: %s', totalUsers)
	logger.info('>> JDBC: %s', config.JDBC)
	logger.info('>> OUTPUT DIR: %s', outputDir)
	logger.info("#########################################################################################################################")
	print "\n"

	success = False
	for attempt in range(10):
		if jdbc == 'crdt':
			success = runOverheadExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		else:
			success = runOverheadExperimentOrig(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		
		if success:
			break
		else:
			logger.error("experiment failed. Retrying...")
			fab.killRunningProcesses()
			execute(fab.cleanOutputFiles, hosts=config.distinct_nodes)

	if not success:
		logger.error("failed to execute experiment after 10 retries. Exiting...")					
		sys.exit()

def runOverheadExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start")
		return False
	
	success = startCoordinatorsLayer(configFile)
	if success == True:
		logger.info('all coordinators are online')       		
	else:
		logger.error("coordination layer failed to start")
		return False   
	
	success = startReplicationLayer(configFile)
	if success == True:
		logger.info('all replicators are online')       		
	else:
		logger.error("replication layer failed to start")
		return False
	
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "true")

	time.sleep(config.TPCC_TEST_TIME+20)	

	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')				
			else:
				isRunning = False
		if isRunning == True:
			attempts += 1
			time.sleep(20)

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeResultCSVFiles(outputDir, totalUsers, 1)	
	logger.info('logs can be found at %s', outputDir)

	return True

def runOverheadExperimentOrig(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start")
		return False
	
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "false")

	time.sleep(config.TPCC_TEST_TIME+20)
	
	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')				
			else:
				isRunning = False
		if isRunning == True:
			attempts += 1
			time.sleep(20)

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeResultCSVFiles(outputDir, totalUsers, 1)	
	logger.info('logs can be found at %s', outputDir)

	return True

################################################################################################		
# SCALABILITY METHODS
################################################################################################		
def runScalabilityExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, numberOfReplicas):
	
	print "\n"
	logger.info("########################################## starting new scalability experiment ##########################################")
	logger.info('>> CONFIG FILE: %s', CONFIG_FILE)
	logger.info('>> DATABASES: %s', config.database_nodes)
	logger.info('>> REPLICATORS: %s', config.replicators_nodes)
	logger.info('>> NUMBER OF EMULATORS: %s', NUMBER_OF_EMULATORS)
	logger.info('>> CLIENTS PER EMULATOR: %s', USERS_PER_EMULATOR)
	logger.info('>> TOTAL USERS: %s', TOTAL_USERS)
	logger.info('>> JDBC: %s', config.JDBC)
	logger.info('>> OUTPUT DIR: %s', OUTPUT_DIR)
	logger.info("#########################################################################################################################")
	print "\n"

	success = False
	for attempt in range(10):
		if config.JDBC == 'crdt':
			success = runScalabilityExperimentCRDT(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, numberOfReplicas)
		else:
			success = runScalabilityExperimentBaseline(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, numberOfReplicas)
		
		if success:
			break
		else:
			logger.error("experiment failed. Retrying...")
			fab.killRunningProcesses()
			execute(fab.cleanOutputFiles, hosts=config.distinct_nodes)

	if not success:
		logger.error("failed to execute experiment after 10 retries. Exiting...")					
		sys.exit()

def runScalabilityExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers, numberOfReplicas):
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start")
		return False
	
	success = startCoordinatorsLayer(configFile)
	if success == True:
		logger.info('all coordinators are online')       		
	else:
		logger.error("coordination layer failed to start")
		return False   
	
	success = startReplicationLayer(configFile)
	if success == True:
		logger.info('all replicators are online')       		
	else:
		logger.error("replication layer failed to start")
		return False
	
	startClientEmulators(configFile, numberEmulators, usersPerEmulator, "true")

	time.sleep(config.TPCC_TEST_TIME+20)
	isRunning = True
	attempts = 0
	while isRunning:
		if attempts >= 6:
			logger.error("checked 6 times if clients were running. Something is probably wrong")
			return False
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			output = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if utils.fabOutputContainsExpression(output, "True"):
				isRunning = True
				logger.info('experiment is still running!')				
			else:
				isRunning = False
		if isRunning == True:
			attempts += 1
			time.sleep(10)
		else:
			break

	logger.info('the experiment has finished!')
	fab.killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeResultCSVFiles(outputDir, totalUsers, numberOfReplicas)	
	logger.info('logs can be found at %s', outputDir)

	return True

def runScalabilityExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers, numberOfReplicas):
	pass

################################################################################################
#   START LAYERS METHODS
################################################################################################
def startDatabaseLayer():
	if config.JDBC == 'crdt':
		with hide('running','output'):
			execute(fab.prepareTPCCDatabase, hosts=config.database_nodes)
			output = execute(fab.startDatabases, hosts=config.database_nodes)
			for key, value in output.iteritems():
				if value == '0':
					logger.warn('database at %s failed to start', key)
					return False
			return True
	elif config.JDBC == 'galera':
		with hide('running','output'):
			execute(fab.prepareTPCCDatabase, hosts=config.database_nodes)
			masterDatabaseReplica = config.database_nodes[0]
			masterList = [masterDatabaseReplica]
			slavesReplicas = config.database_nodes[:]
			slavesReplicas.remove(masterDatabaseReplica) 
			logger.info("%s will bootstrap Galera-Cluster", masterDatabaseReplica)
			logger.info("%s will join after the cluster is online", slavesReplicas)		
			#start master replica (that will bootstrap the cluster)
			output = execute(fab.startDatabasesGalera, True, hosts=masterList)
		for key, value in output.iteritems():
			if utils.fabOutputContainsExpression(output, "0"):		
				logger.error('database at %s failed to start', key)
				return False								

		if len(config.database_nodes) > 1:
			#start remainning nodes
			with hide('running','output'):
				output = execute(fab.startDatabasesGalera, False, hosts=slavesReplicas)
			if utils.fabOutputContainsExpression(output, "0"):		
				logger.error('database at %s failed to start', key)
				return False							
			
		return True
	
	elif config.JDBC == 'cluster':
		with hide('running','output'):
			execute(fab.prepareTPCCDatabase, hosts=config.database_nodes)
			output = execute(fab.startClusterDatabases, hosts=config.database_nodes)
			for key, value in output.iteritems():
				if value == '0':
					logger.warn('database at %s failed to start', key)
					return False
			return True
	else:
		logger.error("unexpected driver: %s", config.JDBC)
		sys.exit()
		
def startCoordinatorsLayer(configFile):
	with hide('running','output'):
		output = execute(fab.startCoordinators, configFile, hosts=config.coordinators_nodes)
		for key, value in output.iteritems():
			if value == '0':
				logger.error('coordinator at %s failed to start', key)
				return False
	return True    			

def startReplicationLayer(configFile):
	with hide('running','output'):
		output = execute(fab.startReplicators, configFile, hosts=config.replicators_nodes)
		for key, value in output.iteritems():
			if value == '0':
				logger.error('replicator at %s failed to start', key)
				return False
	return True    			    			

def startClientEmulators(configFile, emulatorsNumber, clientsPerEmulator, customJDBC):
	with hide('running','output'):
		execute(fab.startTPCCclients, configFile, emulatorsNumber, clientsPerEmulator, customJDBC, hosts=config.emulators_nodes)

################################################################################################
#   HELPER AND "PRIVATE" METHODS
################################################################################################
def prepareCode():
	logger.info('compiling source code')
	with lcd(config.PROJECT_DIR), hide('output','running'):
		local('ant purge tpcc-dist')
	logger.info('uploading distribution to nodes: %s', config.distinct_nodes)
	logger.info('deploying jars, resources and config files')
	with hide('output','running'):
		execute(fab.distributeCode, hosts=config.distinct_nodes)

def downloadLogs(outputDir):
	logger.info('downloading log files')
	with hide('running', 'output'):
		execute(fab.downloadLogsTo, outputDir, hosts=config.distinct_nodes)

def checkGaleraClusterStatus(masterReplicaHost):
	numberOfDatabases = len(config.database_nodes)
	command = 'bin/mysql --defaults-file=my.cnf -u sa -p101010 -e "SHOW STATUS LIKE \'wsrep_cluster_size\';" | grep wsrep'
	output = fab.executeRemoteTerminalCommandAtDir(masterReplicaHost, command, config.GALERA_MYSQL_DIR)	
	logger.debug('cluster output: %s', output)
	return True
	if str(numberOfDatabases) not in output:
		logger.error("cluster was not properly initialized: %s", output)
		return True

	command = 'bin/mysql --defaults-file=my.cnf -u sa -p101010 -e "SHOW STATUS LIKE \'wsrep_ready\';" | grep wsrep_ready'
	output = fab.executeRemoteTerminalCommandAtDir(masterReplicaHost, command, config.GALERA_MYSQL_DIR)	
	if 'ON' not in output:
		logger.error("cluster was not properly initialized: %s", output)
		return True

	return True











