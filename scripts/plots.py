from fabric.api import env, local, lcd, roles, parallel, cd, put, get, execute, settings, abort, hide, task, sudo, run, warn_only
import time
import sys
import logging
import os

logger = logging.getLogger('logger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

def generateLatencyThroughput(dirPath):    
    with lcd(dirPath):        
        for n in NUMBER_USERS_LIST:
            fileName = dirPath + '/' + str(n) + '_clients.result'
            local('cat ' + fileName + ' >> plot_data')
            local('echo \'\' ' ' >> plot_data')
    
        plotFilePath = EXPERIMENTS_DIR + '/latency-throughput.gp' 
        local('gnuplot -e \"data=\'plot_data\'; outputfile=\'plot.eps\'\" ' + plotFilePath)
