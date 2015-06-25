package util.defaults;


import database.util.DatabaseMetadata;
import nodes.NodeConfig;
import nodes.Role;
import nodes.proxy.ProxyConfig;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.exception.ConfigurationLoadException;
import util.parser.DDLParser;
import util.DatabaseProperties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 13/03/15.
 */
public final class Configuration
{
	public static boolean DEBUG_ENABLED;
	public static boolean TRACE_ENABLED;
	public static boolean INFO_ENABLED;

	private static Configuration ourInstance = new Configuration();
	private static Logger LOG;

	private static String CONFIG_FILE;
	private static final String BASE_DIR = "/local/dp.lopes/deploy";

	private Map<Integer, NodeConfig> replicators;
	private Map<Integer, ProxyConfig> proxies;
	private Map<Integer, NodeConfig> coordinators;
	private Map<Integer, DatabaseProperties> databases;
	private DatabaseMetadata databaseMetadata;
	private String databaseName;
	private String schemaFile;
	private int scratchpadPoolSize;
	private StopWatch watch;

	private Configuration()
	{
		LOG = LoggerFactory.getLogger(Configuration.class);
		INFO_ENABLED = LOG.isInfoEnabled();
		TRACE_ENABLED = LOG.isTraceEnabled();
		DEBUG_ENABLED = LOG.isDebugEnabled();

		CONFIG_FILE = System.getProperty("configPath");
		if(Configuration.INFO_ENABLED)
			LOG.info("loading configuration file: {}", CONFIG_FILE);


		this.watch = new StopWatch();
		this.replicators = new HashMap<>();
		this.proxies = new HashMap<>();
		this.coordinators = new HashMap<>();
		this.databases = new HashMap<>();

		try
		{
			watch.start();
			loadConfigurationFile();
		} catch(ConfigurationLoadException e)
		{
			RuntimeUtils.throwRunTimeException("failed to load config file: " + e.getMessage(), ExitCode.XML_ERROR);
		}

		if(!this.checkConfig())
		{
			LOG.error("configuration variables are not properly set");
			RuntimeUtils.throwRunTimeException("configuration variables not properly initialized", ExitCode.XML_ERROR);

		}

		DDLParser parser = new DDLParser(this.schemaFile);
		this.databaseMetadata = parser.parseAnnotations();
		this.watch.stop();

		if(Configuration.TRACE_ENABLED)
			LOG.trace("config file successfully loaded in {} ms", watch.getTime());
	}

	public static Configuration getInstance()
	{
		return ourInstance;
	}

	private void loadConfigurationFile() throws ConfigurationLoadException
	{
		try
		{

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			InputStream stream = new FileInputStream(CONFIG_FILE);
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
				if(n.getNodeName().compareTo("variables") == 0)
					parseVariables(n);
			}

		} catch(ParserConfigurationException | IOException | SAXException e)
		{
			throw new ConfigurationLoadException(e.getMessage());
		}
	}

	private void parseVariables(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		this.scratchpadPoolSize = Integer.parseInt(map.getNamedItem("padPoolSize").getNodeValue());
		this.databaseName = map.getNamedItem("dbName").getNodeValue();
		this.schemaFile = BASE_DIR + "/" + map.getNamedItem("schemaFile").getNodeValue();

		if(Configuration.INFO_ENABLED)
		{
			LOG.info("Scratchpad pool size: {}", this.scratchpadPoolSize);
			LOG.info("Database name: {}", this.databaseName);
			LOG.info("DDL file: {}", this.schemaFile);
		}
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
		String refDatabase = map.getNamedItem("refDatabase").getNodeValue();

		DatabaseProperties props = this.databases.get(Integer.parseInt(refDatabase));

		NodeConfig newCoordinator = new NodeConfig(Role.REPLICATOR, Integer.parseInt(id), host, Integer.parseInt(port),
				props);

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
		String refCoordinator = map.getNamedItem("refCoordinator").getNodeValue();

		NodeConfig replicatorConfig = this.getReplicatorConfigWithIndex(Integer.parseInt(refReplicator));
		DatabaseProperties props = this.databases.get(Integer.parseInt(refDatabase));
		NodeConfig coordinatorConfig = this.getCoordinatorConfigWithIndex(Integer.parseInt(refCoordinator));

		ProxyConfig newProxy = new ProxyConfig(Integer.parseInt(id), host, Integer.parseInt(port), props,
				replicatorConfig, coordinatorConfig);

		proxies.put(Integer.parseInt(id), newProxy);
	}

	public NodeConfig getReplicatorConfigWithIndex(int index)
	{
		return this.replicators.get(index);
	}

	public NodeConfig getCoordinatorConfigWithIndex(int index)
	{
		return this.coordinators.get(index);
	}

	public NodeConfig getProxyConfigWithIndex(int index)
	{
		return this.proxies.get(index);
	}

	public Map<Integer, NodeConfig> getAllReplicatorsConfig()
	{
		return replicators;
	}

	public Map<Integer, ProxyConfig> getProxies()
	{
		return proxies;
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public DatabaseMetadata getDatabaseMetadata()
	{
		return this.databaseMetadata;
	}

	public int getScratchpadPoolSize()
	{
		return this.scratchpadPoolSize;
	}

	private boolean checkConfig()
	{
		return !(this.databaseName == null || this.schemaFile == null || this.proxies.size() == 0 || this.replicators.size() == 0 || this.coordinators.size() == 0);
	}
}

