import xml.etree.ElementTree as ET
from sets import Set
import logging
import utils as utils

logger = logging.getLogger('globalVar_logger')
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
formatter = logging.Formatter('[%(levelname)s] %(message)s')
ch.setFormatter(formatter)
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

IS_LOADED = False

################################################################################################
#   CURRENT CONFIGURATION (the only variables needed to modify between benchmarks)
################################################################################################

#user='dp.lopes'
user='dnl'

#ENVIRONMENT='fct'
ENVIRONMENT='localhost'
#ENVIRONMENT='amazon'
IS_LOCALHOST = True
#IS_LOCALHOST = False

TPCC_WORKLOAD_FILE='workload1'
ANNOTATION_FILE = 'tpcc_ddl_no_sequential_ids.sql'
ENVIRONMENT_FILE= 'env_localhost_default'
TOPOLOGY_FILE=''
TPCC_TEST_TIME=60
FILES_PREFIX=''

################################################################################################
#   PREFIXS AND GLOBAL VARIABLES
################################################################################################
ZOOKEEPER_CLIENT_PORT='2181'
ZOOKEEPER_PORT1 ='2888'
ZOOKEEPER_PORT2 ='3888'
ZOOKEEPER_CFG_FILE='zoo.cfg'
ZOOKEEPER_CONNECTION_STRING = ''
ZOOKEEPER_SERVERS_STRING = ''

MYSQL_PORT='3306'
TOTAL_USERS=0
JDBC=''

ACTIVE_EXPERIMENT=""
prefix_latency_throughput_experiment = "latency-throughput"
prefix_scalability_experiment = "scalability"
prefix_overhead_experiment = "overhead"

################################################################################################
#	COMMANDS AND BASE PATHS
################################################################################################

MYSQL_SHUTDOWN_COMMAND='bin/mysqladmin -u sa --password=101010 --socket=/tmp/mysql.sock shutdown'
MYSQL_START_COMMAND='bin/mysqld_safe --defaults-file=my.cnf --open_files_limit=8192 --max-connections=1500'
BASE_DIR = '/local/' + user
DEPLOY_DIR = BASE_DIR + '/deploy'
ENVIRONMENT_DIR = DEPLOY_DIR + '/environment'
ANNOTATIONS_DIR = DEPLOY_DIR + '/annotations'
TOPOLOGIES_DIR = DEPLOY_DIR + '/topologies'
TPCC_WORKLOADS_DIR = DEPLOY_DIR + '/tpcc'

MYSQL_DIR = BASE_DIR + '/mysql-5.6'
GALERA_MYSQL_DIR = BASE_DIR + '/mysql-5.6-galera'
CLUSTER_MYSQL_DIR = BASE_DIR + '/mysql-cluster'
HOME_DIR = '/home/' + user
LOGS_DIR = HOME_DIR + '/logs'
BACKUPS_DIR = HOME_DIR + '/backups'
PROJECT_DIR = HOME_DIR + '/code'
JARS_DIR = PROJECT_DIR + '/dist/jars'
EXPERIMENTS_DIR = PROJECT_DIR + '/experiments'
ZOOKEEPER_DATA_DIR = BASE_DIR + '/zookeeper'

################################################################################################
#	DATA STRUCTURES
################################################################################################

distinct_nodes = []
database_nodes = []
replicators_nodes = []
coordinators_nodes = []
emulators_nodes = []

# maps between node_id and node_host
database_map = dict()
replicators_map = dict()
coordinators_map = dict()
emulators_map = dict()

# maps between node_id and listening port
# usefull for checking if the layer was correctly initialized
replicatorsIdToPortMap = dict()
coordinatorsIdToPortMap = dict()

################################################################################################
#	METHODS
################################################################################################

# receives full path for config file
def parseTopologyFile(topologyFile):
	logger.info('parsing topology file: %s', topologyFile)
	e = ET.parse(topologyFile).getroot()
	distinctNodesSet = Set()

	global database_map, emulators_map, coordinators_map, replicators_map, replicatorsIdToPortMap, coordinatorsIdToPortMap
	global database_nodes, replicators_nodes, distinct_nodes, coordinators_nodes, emulators_nodes

	distinct_nodes = []
	database_nodes = []
	replicators_nodes = []
	coordinators_nodes = []
	emulators_nodes = []
	database_map = dict()
	replicators_map = dict()
	coordinators_map = dict()
	emulators_map = dict()
	replicatorsIdToPortMap = dict()
	coordinatorsIdToPortMap = dict()

	for database in e.iter('database'):
		dbId = database.get('id')
		dbHost = database.get('dbHost')
		database_nodes.append(dbHost)
		distinctNodesSet.add(dbHost)
		database_map[dbHost] = dbId

	for replicator in e.iter('replicator'):
		replicatorId = replicator.get('id')
		host = replicator.get('host')
		port = replicator.get('port')
		replicators_nodes.append(host)
		distinctNodesSet.add(host)
		replicatorsIdToPortMap[replicatorId] = port
		replicators_map[host] = replicatorId

	for proxy in e.iter('proxy'):
		proxyId = proxy.get('id')
		host = proxy.get('host')
		port = proxy.get('port')
		emulators_nodes.append(host)
		distinctNodesSet.add(host)

	for coordinator in e.iter('coordinator'):
		coordinatorId = coordinator.get('id')
		port = coordinator.get('port')
		host = coordinator.get('host')
		coordinators_nodes.append(host)
		distinctNodesSet.add(host)
		coordinators_map[host] = coordinatorId
		coordinatorsIdToPortMap[coordinatorId] = port

	distinct_nodes = list(distinctNodesSet)

	logger.info('Databases: %s', database_nodes)
	logger.info('Coordinators: %s', coordinators_nodes)
	logger.info('Replicators: %s', replicators_nodes)
	logger.info('Emulators: %s', emulators_nodes)
	logger.info('Distinct nodes: %s', distinct_nodes)

	utils.generateZookeeperConnectionString()
	global IS_LOADED
	IS_LOADED = True







