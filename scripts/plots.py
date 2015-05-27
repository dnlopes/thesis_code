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

################################################################################################
#   MAIN METHODS
################################################################################################

def generatePlotDataFile(outputDir, usersList, isCustomJDBC):
	frame = pd.DataFrame()
	list_ = []
	foundFile = False
	
	for numberUsers in usersList:
		dirName = outputDir + "/" + str(numberUsers) + "user"
		fileName = dirName + "/" + str(numberUsers)
		
		if isCustomJDBC:
			fileName += "users_latency-throughput_crdt.csv"
		else:
			fileName += "users_latency-throughput_orig.csv"

		if not os.path.isfile(fileName):
			logger.warn("file %s not available", fileName)
			continue
		df = pd.read_csv(fileName,index_col=False)
		list_.append(df)
		foundFile = True

	if foundFile:
		frame = pd.concat(list_)
		
		fileName=""
		if isCustomJDBC:
			fileName = outputDir + "/latency-throughput_datapoints_crdt.csv"
		else:
			fileName = outputDir + "/latency-throughput_datapoints_orig.csv"

		frame.to_csv(fileName, sep=",", index=False)

def generateLatencyThroughputPlot(outputDir):
	plotDataFiles = glob.glob(outputDir + "/*.csv")
	logger.info("generating latency-throughput plot with the following datapoints files: %s", plotDataFiles)

	if len(plotDataFiles) == 1:
		generateLatencyThroughputPlot1Line(plotDataFiles)
	elif len(plotDataFiles) == 2:
		generateLatencyThroughputPlot2Line(plotDataFiles)
	else:
		logger.error("unexpected number of csv files to plot graphic")
		sys.exit()

def mergeTemporaryCSVfiles(outputDir, totalUsers, isCustomJDBC):
	logger.info("merging temporary CSV files")
	mergeIterationCSVFiles(outputDir, totalUsers, isCustomJDBC)
	mergeResultCSVFiles(outputDir, totalUsers, isCustomJDBC)
	
################################################################################################
#   "PRIVATE" METHODS
################################################################################################

def mergeIterationCSVFiles(outputDir, totalUsers, isCustomJDBC):
	#tempCSVFiles = glob.glob(outputDir + "/*.iters.temp")
	#logger.info("merging files: %s", tempCSVFiles)
	pass

def mergeResultCSVFiles(outputDir, totalUsers, isCustomJDBC):
	tempCSVFiles = glob.glob(outputDir + "/*.results.temp")
	logger.info("merging files: %s", tempCSVFiles)
	
	frame = pd.DataFrame()
	list_ = []
	for file_ in tempCSVFiles:
		df = pd.read_csv(file_,index_col=None, header=0)
		list_.append(df)

	frame = pd.concat(list_)
	#CSV format: numberOps,avgLatency
	totalOps = frame['numberOps'].sum()
	avgLatency = frame['avgLatency'].mean()
	
	fileContent = "numberOps,avgLatency\n"
	fileContent += str(totalOps) + "," + str(avgLatency)

	if isCustomJDBC:
		fileName = outputDir + "/" + str(totalUsers) + "users_latency-throughput_crdt.csv"
	else:
		fileName = outputDir + "/" + str(totalUsers) + "users_latency-throughput_orig.csv"
	logger.info("creating csv file: %s", fileName)
	f = open(fileName,'w')
	f.write(fileContent)
	f.close()

def generateLatencyThroughputPlot1Line(plotDataFiles):
	logger.info("generating plot with 1 line")
	plotCommand = 'gnuplot -e \"data1=\''
	plotCommand +=plotDataFiles[0]
	plotCommand += '\'; outputfile=\'latency-throughput.eps\'\" '
	plotCommand += config.EXPERIMENTS_DIR + "/" + LATENCY_THROUGHPUT_1LINE
	fab.executeTerminalCommand(plotCommand)
	
def generateLatencyThroughputPlot2Line(plotDataFiles):
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
	fab.executeTerminalCommand(plotCommand)

	

if __name__ == '__main__':
    
    #if len(sys.argv) != 1:
        #print "python plots.py <outputDir>"
        #sys.exit()
        
#    option = sys.argv[1:]
    #outputDir = "/Users/dnlopes/devel/thesis/code/weakdb/experiments/logs/27-05_11h50m35s_latency-throughput/1replica"
    outputDir = "/home/dnl/logs/27-05_11h50m35s_latency-throughput/1replica"
    usersList = [1,2]

    generatePlotDataFile(outputDir, usersList, False)
    generatePlotDataFile(outputDir, usersList, True)
    generateLatencyThroughputPlot(outputDir)

                
    
            
    
        
    
    
    