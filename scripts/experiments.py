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

#NUMBER_REPLICAS=[1,3,5]
NUMBER_REPLICAS=[1]
#JDCBs=['mysql_crdt', "default_jdbc"]
JDCBs=['mysql_crdt']

################################################################################################
#   ALL EXPERIMENTS CONFIGURATIONS
################################################################################################

userListToReplicasNumber = dict()
NUMBER_USERS_LIST_1REPLICA=[1,2]
NUMBER_USERS_LIST_3REPLICA=[3,6,15,30,45,60,90,120]
NUMBER_USERS_LIST_5REPLICA=[5,10,15,30,45,80,120,180]
userListToReplicasNumber[1] = NUMBER_USERS_LIST_1REPLICA
userListToReplicasNumber[3] = NUMBER_USERS_LIST_3REPLICA
userListToReplicasNumber[5] = NUMBER_USERS_LIST_5REPLICA

@task
def runFullLatencyThroughputExperiment(configsFilesBaseDir):
	now = datetime.datetime.now()
	OUTPUT_DIR = config.LOGS_DIR + "/" + now.strftime("%d-%m_%Hh%Mm%Ss_") + config.prefix_latency_throughput_experiment
	DIR_TO_DOWNLOAD = OUTPUT_DIR

	# first cycle, iteration over the number of replicas
	for numberOfReplicas in NUMBER_REPLICAS:		
		USERS_LIST = userListToReplicasNumber.get(numberOfReplicas)	 
		CONFIG_FILE = configsFilesBaseDir +'/tpcc_cluster_' + str(numberOfReplicas) + 'node.xml'
		if config.IS_LOCALHOST == True:
			CONFIG_FILE = configsFilesBaseDir +'/tpcc_localhost_' + str(numberOfReplicas) + 'node.xml'
		
		OUTPUT_DIR += "/" + str(numberOfReplicas) + "replica"
		OUTPUT_ROOT_DIR = OUTPUT_DIR
	  
		config.parseConfigFile(CONFIG_FILE)
		killRunningProcesses()
		prepareCode()

		logger.info("starting tests with %d replicas", numberOfReplicas)
		# second cycle, use different jdbc to run experiment
		for jdbc in JDCBs:
			USE_CUSTOM_JDBC = 'true'
			if jdbc == 'mysql_crdt':
				USE_CUSTOM_JDBC='true'
			else:
				USE_CUSTOM_JDBC='false'		
			# third cycle, use different number of users per run
			for numberOfUsers in USERS_LIST:
				config.TOTAL_USERS = numberOfUsers
				OUTPUT_DIR = OUTPUT_ROOT_DIR + "/" + str(numberOfUsers) + "user"
				with hide('output','running','warnings'),settings(warn_only=True):
					local("mkdir -p " + OUTPUT_DIR + "/logs")
				TOTAL_USERS = numberOfUsers
				NUMBER_OF_EMULATORS = len(config.replicators_nodes)
				USERS_PER_EMULATOR = TOTAL_USERS / NUMBER_OF_EMULATORS
				runLatencyThroughputExperiment(OUTPUT_DIR, CONFIG_FILE, USE_CUSTOM_JDBC, NUMBER_OF_EMULATORS, USERS_PER_EMULATOR, TOTAL_USERS)
				logger.info('moving to the next iteration!')
				
			logger.info('generating plot data file for experiment with %s replicas and %s users', numberOfReplicas, USERS_LIST)
			plots.generatePlotDataFile(OUTPUT_ROOT_DIR, USERS_LIST, USE_CUSTOM_JDBC)

		logger.info("generating plot graphic for experience with %s replicas", numberOfReplicas)
		plots.generateLatencyThroughputPlot(OUTPUT_ROOT_DIR)
	
	logger.info("all experiments have finished!")

	scpCommand = "scp -r -P 12034 dp.lopes@di110.di.fct.unl.pt:"
	scpCommand += DIR_TO_DOWNLOAD
	scpCommand += "/Users/dnlopes/devel/thesis/code/weakdb/experiments/logs"
	logger.info("###########################################################################################")
	logger.info("use the following command to copy the logs directories:")
	logger.info(scpCommand)
	logger.info("###########################################################################################")
	print "\n"
	logger.info("Goodbye.")

def runLatencyThroughputExperiment(outputDir, configFile, customJDBC, numberEmulators, usersPerEmulator, totalUsers):
	
	print "\n"
	logger.info("########################################## starting new experiment ##########################################")
	logger.info('>> CONFIG FILE: %s', configFile)
	logger.info('>> DATABASES: %s', config.database_nodes)
	logger.info('>> REPLICATORS: %s', config.replicators_nodes)
	logger.info('>> NUMBER OF EMULATORS: %s', numberEmulators)
	logger.info('>> CLIENTS PER EMULATOR: %s', usersPerEmulator)
	logger.info('>> TOTAL USERS: %s', totalUsers)
	logger.info('>> CUSTOM JDBC: %s', customJDBC)
	logger.info('>> OUTPUT DIR: %s', outputDir)
	logger.info("#############################################################################################################")
	print "\n"

	if customJDBC == 'true':
		runLatencyThroughputExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)
	else:
		runLatencyThroughputExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers)

def runLatencyThroughputExperimentCRDT(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	logger.info("starting database layer")
	
	success = startDatabaseLayer()
	if success == True:		
		logger.info("all databases instances are online") 
	else:		
		logger.error("database layer failed to start. Exiting")
		sys.exit()
	
	success = startCoordinatorsLayer(configFile)
	if success == True:
		logger.info('all coordinators are online')       		
	else:
		logger.error("coordination layer failed to start. Exiting")
		sys.exit()   
	
	success = startReplicationLayer(configFile)
	if success == True:
		logger.info('all replicators are online')       		
	else:
		logger.error("replication layer failed to start. Exiting")
		sys.exit()
	
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
	killRunningProcesses()
	downloadLogs(outputDir)
	plots.mergeTemporaryCSVfiles(outputDir, totalUsers, False)	
	logger.info('logs can be found at %s', outputDir)    

def runLatencyThroughputExperimentBaseline(outputDir, configFile, numberEmulators, usersPerEmulator, totalUsers):
	pass        

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
		logger.debug("outputs: %s", output)
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
#   HELPER METHODS
################################################################################################

def prepareCode():
	logger.info('compiling source code')
	with lcd(config.PROJECT_DIR), hide('output','running'):
		local('ant purge tpcc-dist')
	logger.info('uploading distribution to nodes: %s', config.distinct_nodes)
	logger.info('deploying jars, resources and config files')
	with hide('output','running'):
		execute(fab.distributeCode, hosts=config.distinct_nodes)

def killRunningProcesses():
	logger.info('cleaning running processes')    
	with hide('running','output','warnings'):
		execute(fab.stopJava, hosts=config.distinct_nodes)
		time.sleep(1)
		execute(fab.stopMySQL, hosts=config.database_nodes)
		time.sleep(1)
		execute(fab.stopJava, hosts=config.distinct_nodes)
		time.sleep(1)

def downloadLogs(outputDir):
	logger.info('downloading log files')
	execute(fab.downloadLogsTo, outputDir, hosts=config.distinct_nodes)  


