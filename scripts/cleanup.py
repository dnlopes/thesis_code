#####################################
##### TPCW Deployment Script    #####
##### Author: David Lopes       #####
##### Nova University of Lisbon #####
##### Last update: April, 2015  #####
#####################################

import subprocess
import defaults
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def cleanTPCW():
    logger.info('cleaning tpcw distribution')
    cmd = "rm -rf " + defaults.TOMCAT6_DIR + "/webapps/tpcw*"
    subprocess.call(cmd, shell=True)
    subprocess.check_output(["javac", "-classpath", defaults.COMPILE_CLASSPATH, "-d", "/dist", defaults.SOURCE_DIR])

cleanTPCW()

