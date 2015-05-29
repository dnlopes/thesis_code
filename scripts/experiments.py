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

logger = logging.getLogger('expLogger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)


################################################################################################
#   CURRENT CONFIGURATION (the only variables needed to modify between benchmarks)
################################################################################################

SCALABILITY_USERS_PER_REPLICA=8
#SCALABILITY_NUMBER_REPLICAS=[1,2,3,4,5,6,7]
SCALABILITY_NUMBER_REPLICAS=[1,2]
SCALABILITY_JDCBs=['crdt']
#NUMBER_REPLICAS=[1,3,5]
NUMBER_REPLICAS=[1]
#JDCBs=['mysql_crdt', "default_jdbc"]
JDCBs=['crdt']

################################################################################################
#   ALL EXPERIMENTS CONFIGURATIONS
################################################################################################

userListToReplicasNumber = dict()
NUMBER_USERS_LIST_1REPLICA=[1]
NUMBER_USERS_LIST_3REPLICA=[3,6,15,30,45,60,90,120,150]
NUMBER_USERS_LIST_5REPLICA=[5,10,15,30,45,80,120,180,240]
userListToReplicasNumber[1] = NUMBER_USERS_LIST_1REPLICA
userListToReplicasNumber[3] = NUMBER_USERS_LIST_3REPLICA
userListToReplicasNumber[5] = NUMBER_USERS_LIST_5REPLICA

@task
def runFullLatencyThroughputExperiment(configsFilesBaseDir):
	config.ACTIVE_EXPERIMENT = config.prefix_latency_throughput_experiment
	now = datetime.datetime.now()
	ROOT_OUTPUT_DIR = config.LOGS_DIR + "/" + now.strftime("%d-%m_%Hh%Mm%Ss_") + config.prefix_latency_throughput_experiment

	# first cycle, iteration over the number of replicas
	for numberOfReplicas in NUMBER_REPLICAS:		
		USERS_LIST = userListToReplicasNumber.get(numberOfReplicas)	 
		CONFIG_FILE = configsFilesBaseDir +'/tpcc_cluster_' + str(numberOfReplicas) + 'node.xml'
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
				NUMBER_OF_EMULATORS = len(config.replicators_nodes)
				USERS_PER_EMULATOR = TOTAL_USERS / NUMBER_OF_EMULATORS
				runLatencyThroughputExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS)
				logger.info('moving to the next iteration!')
				
			logger.info('generating plot data file for experiment with %s replicas and %s users', numberOfReplicas, USERS_LIST)
			plots.generatePlotDataFile(REPLICA_OUTPUT_DIR, USERS_LIST)

		logger.info("generating plot graphic for experience with %s replicas", numberOfReplicas)
		plots.generateLatencyThroughputPlot(REPLICA_OUTPUT_DIR)
	
	if not config.IS_LOCALHOST:
		scpCommand = "scp -r -P 12034 dp.lopes@di110.di.fct.unl.pt:"
		scpCommand += ROOT_OUTPUT_DIR
		scpCommand += " /Users/dnlopes/devel/thesis/code/weakdb/experiments/logs"
		print "\n"		
		logger.info("###########################################################################################")
		logger.info("all experiments have finished!")		
		logger.info("use the following command to copy the logs directories:")
		logger.info(scpCommand)
		logger.info("###########################################################################################")
	print "\n"
	logger.info("Goodbye.")

@task
def runFullScalabilityExperiment(configsFilesBaseDir):
	config.ACTIVE_EXPERIMENT = prefix_scalability_experiment	
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
	plots.generateScalabilityPlot(ROOT_OUTPUT_DIR, SCALABILITY_NUMBER_REPLICAS, jdbcDriversList)

################################################################################################
#   START LAYERS METHODS
################################################################################################

def startDatabaseLayer():
	with hide('running','output'):
		execute(fab.prepareTPCCDatabase, hosts=config.database_nodes)
		output = execute(fab.startDatabases, hosts=config.database_nodes)
		for key, value in output.iteritems():
			if value == '0':
				logger.error('database at %s failed to start', key)
				return False
		return True
			
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

def runScalabilityExperiment(OUTPUT_DIR, CONFIG_FILE, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS, numberOfReplicas):
	
	print "\n"
	logger.info("########################################## starting new Scalability experiment ##########################################")
	logger.info('>> CONFIG FILE: %s', configFile)
	logger.info('>> DATABASES: %s', config.database_nodes)
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
		if config.JDBC == 'crdt':
			success = runScalabilityExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers, numberOfReplicas)
		else:
			success = runScalabilityExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers, numberOfReplicas)
		
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

	time.sleep(config.TPCC_TEST_TIME+30)
	isRunning = True
	while isRunning:
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			stillRunning = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if stillRunning == True:
				isRunning = True
				logger.info('experiment is still running!')                
			else:
				isRunning = False
		if isRunning == True:
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
	for attempt in range(10):
		if config.JDBC == 'crdt':
			success = runLatencyThroughputExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		else:
			success = runLatencyThroughputExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
		
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

	time.sleep(config.TPCC_TEST_TIME+30)
	isRunning = True
	while isRunning:
		logger.info('checking experiment status...')   
		with hide('running', 'output'):
			stillRunning = execute(fab.areClientsRunning, numberEmulators, hosts=config.emulators_nodes)
			if stillRunning == True:
				isRunning = True
				logger.info('experiment is still running!')                
			else:
				isRunning = False
		if isRunning == True:
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
	pass        


