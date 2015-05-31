import sys
import logging
import glob
import pandas as pd
import configParser as config
import os.path
import fabfile as fab

logger = logging.getLogger('plotsLogger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

################################################################################################
#   VARIABLES
################################################################################################

LATENCY_THROUGHPUT_1LINE ="latency-throughput_1line.gp"
LATENCY_THROUGHPUT_2LINE ="latency-throughput_2line.gp"
SCALABILITY_1LINE = "scalability_1line.gp"
SCALABILITY_2LINE = "scalability_2line.gp"

################################################################################################
#   MAIN METHODS
################################################################################################

def generateScalabilityPlot(outputDir, numberReplicasList, jdbcDriversList):
	
	for driver in jdbcDriversList:
		logger.debug("generating scalability plot data for driver: %s", driver)
		generateScalabilityLineForJDBC(outputDir, driver, numberReplicasList)

	logger.info("generating scalability plot for %s drivers: ", jdbcDriversList)
	plotDataFiles = glob.glob(outputDir + "/*.csv")

	if len(plotDataFiles) == 1:
		generateScalabilityPlot1Line(plotDataFiles, outputDir)
	elif len(plotDataFiles) == 2:
		generateScalabilityPlot12ine(plotDataFiles, outputDir)
	else:
		logger.error("unexpected number of csv files to plot graphic")
		sys.exit()

def generateScalabilityPlot1Line(plotDataFiles, outputDir):
	logger.info("generating plot with 1 line")
	plotCommand = 'gnuplot -e \"data1=\''
	plotCommand +=plotDataFiles[0]
	plotCommand += '\'; outputfile=\'scalability.eps\'\" '
	plotCommand += config.EXPERIMENTS_DIR + "/" + SCALABILITY_1LINE
	fab.executeTerminalCommandAtDir(plotCommand, outputDir)

def generateScalabilityPlot12ine(plotDataFiles, outputDir):
	pass	

def generateScalabilityLineForJDBC(outputDir, jdbcDriver, numberReplicasList):
	csvFiles = glob.glob(outputDir + "/*.results.temp")

	frame = pd.DataFrame()
	list_ = []
	foundFile = False
	for replicaNum in numberReplicasList:
		aDir = outputDir + "/" + str(replicaNum) + "replica"
		fileSufix = jdbcDriver + ".csv"
		fullDir = aDir + "/*" + fileSufix
		csvFiles = glob.glob(fullDir)		
		
		if len(csvFiles) != 1:
			logger.error("there should exist exactly one CSV file for each jdcb driver per replica folder at: %s", fullDir)
			sys.exit()

		csvFileName = csvFiles[0]
		df = pd.read_csv(csvFileName,index_col=None, header=0)
		list_.append(df)
		foundFile = True

	if foundFile:
		frame = pd.concat(list_)
		fileName = outputDir + "/scalability_datapoints_"
		fileName += config.JDBC
		fileName +=".csv"
		frame.to_csv(fileName, sep=",", index=False)

def generatePlotDataFile(outputDir, usersList):
	frame = pd.DataFrame()
	list_ = []
	foundFile = False
	
	for numberUsers in usersList:
		dirName = outputDir + "/" + str(numberUsers) + "user"
		fileName = dirName + "/" + str(numberUsers)
		
		fileName += "users_latency-throughput_"
		fileName += config.JDBC
		fileName +=".csv"
		
		if not os.path.isfile(fileName):
			logger.warn("file %s not available", fileName)
			continue
		df = pd.read_csv(fileName,index_col=False)
		list_.append(df)
		foundFile = True

	if foundFile:
		frame = pd.concat(list_)		
		fileName = outputDir + "/latency-throughput_datapoints_"
		fileName += config.JDBC
		fileName +=".csv"

		frame.to_csv(fileName, sep=",", index=False)

def generateLatencyThroughputPlot(outputDir):
	plotDataFiles = glob.glob(outputDir + "/*.csv")
	logger.info("generating latency-throughput plot with the following datapoints files: %s", plotDataFiles)

	if len(plotDataFiles) == 1:
		generateLatencyThroughputPlot1Line(plotDataFiles, outputDir)
	elif len(plotDataFiles) == 2:
		generateLatencyThroughputPlot2Line(plotDataFiles, outputDir)
	else:
		logger.error("unexpected number of csv files to plot graphic")
		sys.exit()

def mergeTemporaryCSVfiles(outputDir, totalUsers, numberOfReplicas):
	logger.info("merging temporary CSV files")
	mergeIterationCSVFiles(outputDir, totalUsers)
	mergeResultCSVFiles(outputDir, totalUsers, numberOfReplicas)
	
################################################################################################
#   "PRIVATE" METHODS
################################################################################################

def mergeIterationCSVFiles(outputDir, totalUsers):
	#tempCSVFiles = glob.glob(outputDir + "/*.iters.temp")
	#logger.info("merging files: %s", tempCSVFiles)
	pass

def mergeResultCSVFiles(outputDir, totalUsers, numberOfReplicas):
	tempCSVFiles = glob.glob(outputDir + "/*.results.temp")
	logger.info("merging files: %s", tempCSVFiles)
	
	frame = pd.DataFrame()
	list_ = []
	for file_ in tempCSVFiles:
		df = pd.read_csv(file_,index_col=None, header=0)
		list_.append(df)

	frame = pd.concat(list_)
	#CSV format: numberOps,opsPerSecond,avgLatency,numberOfReplicas,usersNumber,usersPerEmulator
	totalOps = frame['numberOps'].sum()
	tpmc = frame['tpmc'].sum()
	avgLatency = frame['avgLatency'].mean()
	abortRate = frame['abortrate'].mean()
	
	opsPerSecond = totalOps / config.TPCC_TEST_TIME
	usersPerEmulator = totalUsers / numberOfReplicas
	fileContent = "numberOps,opsPerSecond,avgLatency,numberOfReplicas,usersNumber,usersPerEmulator,abortrate,tpmc\n"
	fileContent += str(totalOps) + "," + str(opsPerSecond) + "," + str(avgLatency) + "," + str(numberOfReplicas) + "," + str(totalUsers) + "," + str(usersPerEmulator) + "," + str(abortRate) + "," + str(tpmc)

	fileName = outputDir + "/" + str(totalUsers) + config.ACTIVE_EXPERIMENT + "_"
	fileName += config.JDBC
	fileName +=".csv"
	
	logger.info("creating csv file: %s", fileName)
	f = open(fileName,'w')
	f.write(fileContent)
	f.close()

def generateLatencyThroughputPlot1Line(plotDataFiles, outputDir):
	logger.info("generating plot with 1 line")
	plotCommand = 'gnuplot -e \"data1=\''
	plotCommand +=plotDataFiles[0]
	plotCommand += '\'; outputfile=\'latency-throughput.eps\'\" '
	plotCommand += config.EXPERIMENTS_DIR + "/" + LATENCY_THROUGHPUT_1LINE
	fab.executeTerminalCommandAtDir(plotCommand, outputDir)
	
def generateLatencyThroughputPlot2Line(plotDataFiles, outputDir):
	logger.info("generating plot with 2 line")
	plotCommand = 'gnuplot -e \"data1=\''
	if 'crdt' in plotDataFiles[0]:
		plotCommand += plotDataFiles[0]
	elif 'crdt' in plotDataFiles[1]:
		plotCommand += plotDataFiles[1]		
	plotCommand += '\'; data2=\''
	if 'orig' in plotDataFiles[0]:
		plotCommand += plotDataFiles[0]
	elif 'orig' in plotDataFiles[1]:
		plotCommand += plotDataFiles[1]	
	plotCommand +='\' ; outputfile=\'latency-throughput.eps\'\" '
	plotCommand += config.EXPERIMENTS_DIR + "/" + LATENCY_THROUGHPUT_2LINE
	fab.executeTerminalCommandAtDir(plotCommand, outputDir)





