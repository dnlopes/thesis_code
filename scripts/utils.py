import time
import sys
import logging
import subprocess, signal
import os
import plots
import configParser as config
import fabfile as fab



def lineContainsExpression(line, expression):
	if expression in line:
		return True
	else:
		return False

def fabOutputContainsExpression(fabOutput, expression):
	for line in fabOutput.iteritems():
		if lineContainsExpression(line, expression):
			return True

	return False