from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run, warn_only
import time
import sys
import logging
import glob
import pandas as pd
import configParser as config


logger = logging.getLogger('plotsLogger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

def generateLatencyThroughput(outputDir, usersList):
	for n in usersList:
		fileName = outputDir + '/' + str(n) + '_clients.result'
		local('cat ' + fileName + ' >> latency-throughput_data')
		local('echo \'\' ' ' >> plot_data')
	plotFilePath = config.EXPERIMENTS_DIR + '/latency-throughput.gp' 
	local('gnuplot -e \"data=\'latency-throughput_data\'; outputfile=\'latency-throughput.eps\'\" ' + plotFilePath)

def mergeTemporaryCSVfiles(outputDir):
	logger.info("merging temporary CSV files")
	mergeIterationCSVFiles(outputDir)
	mergeResultCSVFiles(outputDir)
	
def mergeIterationCSVFiles(outputDir):
	tempCSVFiles = glob.glob(outputDir + "/*.iters.temp")
	logger.info("merging files: %s", tempCSVFiles)

def mergeResultCSVFiles(outputDir):
	tempCSVFiles = glob.glob(outputDir + "/*.result.temp")
	logger.info("merging files: %s", tempCSVFiles)
	

def processLogFiles():
    numberClients = len(proxies_map)
    totalOps = 0
    totalLatency = 0    
    prefix = LOGS_DIR + '/' + LOG_FILE_DIR 

    for x in xrange(1, numberClients+1):
        fileName = 'client_' + str(x) + '.result.temp'
        filePath = prefix + '/' + fileName
        lines = [line.strip() for line in open(filePath)]        
        splitted = lines[0].split(',')        
        parcialOps = int(splitted[0])
        totalOps += parcialOps
        parcialLatency = int(splitted[1])
        totalLatency += parcialLatency

    avgLatency = totalLatency / numberClients
    
    fileName = prefix + '/' + str(TOTAL_USERS) + '_clients.result'
               
    #OPS LATENCY CLIENTS
    with lcd(prefix):
        stringToWrite = str(totalOps) + ',' + str(avgLatency)
        f = open(fileName,'w')
        f.write(stringToWrite)
        f.close() # you can omit in most cases as the destructor will call if

        local('mkdir -p temp')
        local('mv *.temp temp/')
