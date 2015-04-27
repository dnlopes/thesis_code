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

def parseConfigInput(file):

	varsMap = dict()
	f = open(file)
	for line in f:
		if not line.startswith('#'):
			splitted = line.split('=')
			varsMap[splitted[0]] = splitted[1].rstrip()
	f.close()    
	return varsMap




