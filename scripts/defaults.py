#####################################
##### TPCW Deployment Script    #####
##### Author: David Lopes       #####
##### Nova University of Lisbon #####
##### Last update: April, 2015  #####
#####################################


BASE_DIR = "/var/tmp"

TOMCAT6_DIR = BASE_DIR + "/layers/tomcat6"

MAIN_DIR = BASE_DIR + "/weakdb"
SOURCE_DIR = MAIN_DIR + "/src"
DIST_DIR = BASE_DIR + "/dist"
APPS_DIR = MAIN_DIR + "/applications"
TPCW_DIR = APPS_DIR+ "/tpcw"
JARS_DIR = MAIN_DIR + '/lib'

COMPILE_CLASSPATH = JARS_DIR + '/*'
