package common;


import common.database.util.DatabaseMetadata;
import common.nodes.NodeConfig;
import common.nodes.Role;
import common.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import common.util.exception.ConfigurationLoadException;
import common.parser.DDLParser;
import server.agents.coordination.zookeeper.EZKCoordinationExtension;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * Created by dnlopes on 13/03/15.
 */
public final class Configuration
{

	private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
	private volatile static boolean IS_CONFIGURED = false;
	private static Configuration instance;

	public static String TOPOLOGY_FILE;
	public static String DDL_ANNOTATIONS_FILE;
	public static String ENVIRONMENT_FILE;

	private Map<Integer, NodeConfig> replicators;
	private Map<Integer, NodeConfig> proxies;
	private Map<Integer, NodeConfig> coordinators;
	private Map<Integer, DatabaseProperties> databases;
	private DatabaseMetadata databaseMetadata;

	private Configuration(String topologyFile, String annotationsFile, String environmentFile)
	{
		if(topologyFile == null)
			RuntimeUtils.throwRunTimeException("topology file path not set", ExitCode.NOINITIALIZATION);
		if(annotationsFile == null)
			RuntimeUtils.throwRunTimeException("annotations file path not set", ExitCode.NOINITIALIZATION);
		if(environmentFile == null)
			RuntimeUtils.throwRunTimeException("configs file path not set", ExitCode.NOINITIALIZATION);

		TOPOLOGY_FILE = topologyFile;
		DDL_ANNOTATIONS_FILE = annotationsFile;
		ENVIRONMENT_FILE = environmentFile;

		loadEnvironment();
		IS_CONFIGURED = true;
	}

	public static Configuration getInstance()
	{
		return instance;
	}

	public static synchronized void setupConfiguration(String topologyFile, String annotationsFile, String configsFile)
	{
		if(IS_CONFIGURED)
			LOG.warn("setupConfiguration called twice");
		else
			instance = new Configuration(topologyFile, annotationsFile, configsFile);
	}

	private void loadEnvironment()
	{
		if(LOG.isInfoEnabled())
			LOG.info("loading configuration file: {}", TOPOLOGY_FILE);

		this.replicators = new HashMap<>();
		this.proxies = new HashMap<>();
		this.coordinators = new HashMap<>();
		this.databases = new HashMap<>();

		try
		{
			loadTopology();
			loadAnnotations();
			loadConfigurations();
		} catch(ConfigurationLoadException e)
		{
			RuntimeUtils.throwRunTimeException("failed to configuration: " + e.getMessage(), ExitCode.XML_ERROR);
		}

		if(!this.checkConfig())
		{
			LOG.error("environment configuration is not properly set");
			RuntimeUtils.throwRunTimeException("environment configuration is not properly set", ExitCode.XML_ERROR);

		}

		if(LOG.isTraceEnabled())
			LOG.trace("config file successfully loaded");

		printEnvironment();
	}

	private void printEnvironment()
	{
		if(LOG.isInfoEnabled())
		{
			LOG.info("environment:" + EnvironmentDefaults.DATABASE_NAME_VAR + "=" + Environment.DATABASE_NAME);
			LOG.info(
					"environment:" + EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR + "=" + Environment
							.COMMIT_PAD_POOL_SIZE);
			LOG.info(
					"environment:" + EnvironmentDefaults.EZK_EXTENSION_CODE_VAR + "=" + Environment
							.EZK_EXTENSION_CODE);
			LOG.info(
					"environment:" + EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR + "=" + Environment
							.EZK_CLIENTS_POOL_SIZE);
			LOG.info("environment:" + EnvironmentDefaults.OPTIMIZE_BATCH_VAR + "=" + Environment.OPTIMIZE_BATCH);
		}
	}

	private void loadConfigurations() throws ConfigurationLoadException
	{

		Properties prop = new Properties();

		try
		{
			prop.load(new FileInputStream(ENVIRONMENT_FILE));

			if(prop.containsKey(EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR))
				Environment.COMMIT_PAD_POOL_SIZE = Integer.parseInt(
						prop.getProperty(EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR));
			else
				Environment.COMMIT_PAD_POOL_SIZE = EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_DEFAULT;

			if(prop.containsKey(EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR))
				Environment.EZK_CLIENTS_POOL_SIZE = Integer.parseInt(
						prop.getProperty(EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR));
			else
				Environment.EZK_CLIENTS_POOL_SIZE = EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_DEFAULT;

			if(prop.containsKey(EnvironmentDefaults.OPTIMIZE_BATCH_VAR))
				Environment.OPTIMIZE_BATCH = Boolean.parseBoolean(
						prop.getProperty(EnvironmentDefaults.OPTIMIZE_BATCH_VAR));
			else
				Environment.OPTIMIZE_BATCH = EnvironmentDefaults.OPTIMIZE_BATCH_DEFAULT;

			if(prop.containsKey(EnvironmentDefaults.DATABASE_NAME_VAR))
				Environment.DATABASE_NAME = prop.getProperty(EnvironmentDefaults.DATABASE_NAME_VAR);
			else
				RuntimeUtils.throwRunTimeException("missing mandatory database name parameter in environment file",
						ExitCode.MISSING_IMPLEMENTATION);
			if(prop.containsKey(EnvironmentDefaults.EZK_EXTENSION_CODE_VAR))
				Environment.EZK_EXTENSION_CODE = prop.getProperty(EnvironmentDefaults.EZK_EXTENSION_CODE_VAR);
			else
				RuntimeUtils.throwRunTimeException("missing mandatory ezk-extension-code-dir in environment file",
						ExitCode.MISSING_IMPLEMENTATION);

		} catch(IOException e)
		{
			LOG.error("failed to load workload file. Exiting...");
			System.exit(1);
		}
	}

	private void loadTopology() throws ConfigurationLoadException
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			InputStream stream = new FileInputStream(TOPOLOGY_FILE);
			//Document doc = dBuilder.parse(this.getClass().getResourceAsStream(CONFIG_FILE));
			Document doc = dBuilder.parse(stream);
			//optional, but recommended
			doc.getDocumentElement().normalize();

			NodeList rootList = doc.getElementsByTagName("config");
			Node config = rootList.item(0);
			NodeList nodeList = config.getChildNodes();

			for(int i = 0; i < nodeList.getLength(); i++)
			{
				Node n = nodeList.item(i);
				if(n.getNodeType() != Node.ELEMENT_NODE)
					continue;

				if(n.getNodeName().compareTo("topology") == 0)
					parseTopology(n);
			}

		} catch(ParserConfigurationException | IOException | SAXException e)
		{
			throw new ConfigurationLoadException(e.getMessage());
		}
	}

	private void loadAnnotations()
	{
		DDLParser parser = new DDLParser(DDL_ANNOTATIONS_FILE);
		this.databaseMetadata = parser.parseAnnotations();

		if(LOG.isTraceEnabled())
			LOG.trace("config file successfully loaded");
	}

	private void parseTopology(Node node)
	{
		NodeList nodeList = node.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node n = nodeList.item(i);
			if(n.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(n.getNodeName().compareTo("replicators") == 0)
				parseReplicators(n);
			if(n.getNodeName().compareTo("coordinators") == 0)
				parseCoordinators(n);
			if(n.getNodeName().compareTo("proxies") == 0)
				parseProxies(n);
			if(n.getNodeName().compareTo("databases") == 0)
				parseDatabases(n);
		}
	}

	private void parseDatabases(Node node)
	{
		NodeList nodeList = node.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node n = nodeList.item(i);
			if(n.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(n.getNodeName().compareTo("database") == 0)
				createDatabase(n);
		}
	}

	private void createDatabase(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String dbHost = map.getNamedItem("dbHost").getNodeValue();
		String dbPort = map.getNamedItem("dbPort").getNodeValue();
		String dbUser = map.getNamedItem("dbUser").getNodeValue();
		String dbPwd = map.getNamedItem("dbPwd").getNodeValue();

		DatabaseProperties prop = new DatabaseProperties(dbUser, dbPwd, dbHost, Integer.parseInt(dbPort));
		databases.put(Integer.parseInt(id), prop);
	}

	private void parseReplicators(Node node)
	{
		NodeList nodeList = node.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node n = nodeList.item(i);
			if(n.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(n.getNodeName().compareTo("replicator") == 0)
				createReplicator(n);
		}
	}

	private void parseProxies(Node node)
	{
		NodeList nodeList = node.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node n = nodeList.item(i);
			if(n.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(n.getNodeName().compareTo("proxy") == 0)
				createProxy(n);
		}
	}

	private void parseCoordinators(Node node)
	{
		NodeList nodeList = node.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node n = nodeList.item(i);
			if(n.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if(n.getNodeName().compareTo("coordinator") == 0)
				createCoordinator(n);
		}
	}

	private void createCoordinator(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String host = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();

		NodeConfig newCoordinator;

		if(port != null)
			newCoordinator = new NodeConfig(Role.COORDINATOR, Integer.parseInt(id), host, Integer.parseInt(port),
					null);
		else
			newCoordinator = new NodeConfig(Role.COORDINATOR, Integer.parseInt(id), host,
					EZKCoordinationExtension.ZookeeperDefaults.ZOOKEEPER_DEFAULT_PORT, null);

		coordinators.put(Integer.parseInt(id), newCoordinator);
	}

	private void createReplicator(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String host = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();
		String refDatabase = map.getNamedItem("refDatabase").getNodeValue();

		DatabaseProperties props = this.databases.get(Integer.parseInt(refDatabase));

		NodeConfig newReplicator = new NodeConfig(Role.REPLICATOR, Integer.parseInt(id), host, Integer.parseInt(port),
				props);

		replicators.put(Integer.parseInt(id), newReplicator);
	}

	private void createProxy(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String host = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();
		String refDatabase = map.getNamedItem("refDatabase").getNodeValue();
		String refReplicator = map.getNamedItem("refReplicator").getNodeValue();

		NodeConfig replicatorConfig = this.getReplicatorConfigWithIndex(Integer.parseInt(refReplicator));
		DatabaseProperties props = this.databases.get(Integer.parseInt(refDatabase));

		NodeConfig newProxy = new NodeConfig(Role.PROXY, Integer.parseInt(id), host, Integer.parseInt(port), props,
				replicatorConfig);

		proxies.put(Integer.parseInt(id), newProxy);
	}

	public NodeConfig getReplicatorConfigWithIndex(int index)
	{
		return this.replicators.get(index);
	}

	public NodeConfig getProxyConfigWithIndex(int index)
	{
		return this.proxies.get(index);
	}

	public Map<Integer, NodeConfig> getAllReplicatorsConfig()
	{
		return replicators;
	}

	public DatabaseMetadata getDatabaseMetadata()
	{
		return this.databaseMetadata;
	}

	public int getReplicatorsCount()
	{
		return this.replicators.size();
	}

	private boolean checkConfig()
	{
		return !(this.databases.size() == 0 || this.proxies.size() == 0 || this.replicators.size() == 0 || this
				.coordinators.size() == 0);
	}

	public String getZookeeperConnectionString()
	{
		StringBuffer buffer = new StringBuffer();

		Iterator<NodeConfig> coordinatorConfigsIt = this.coordinators.values().iterator();
		while(coordinatorConfigsIt.hasNext())
		{
			buffer.append(coordinatorConfigsIt.next().getHost());
			if(coordinatorConfigsIt.hasNext())
				buffer.append(",");
		}

		return buffer.toString();
	}

}

