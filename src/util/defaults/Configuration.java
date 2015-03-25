package util.defaults;


import database.util.DatabaseMetadata;
import network.coordinator.CoordinatorConfig;
import network.proxy.Proxy;
import network.proxy.ProxyConfig;
import network.replicator.ReplicatorConfig;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.parser.DDLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 13/03/15.
 */
public final class Configuration
{

	private static Configuration ourInstance = new Configuration();
	public static Proxy PROXY;
	private static Logger LOG;

	private static final String CONFIG_FILE = "/Users/dnlopes/devel/thesis/code/weakdb/resources/config.xml";

	private Map<Integer, ReplicatorConfig> replicators;
	private Map<Integer, ProxyConfig> proxies;
	private Map<Integer, CoordinatorConfig> coordinators;
	private DatabaseMetadata databaseMetadata;
	private String databaseName;
	private String schemaFile;
	private int scratchpadPoolSize;
	private StopWatch watch;

	private Configuration()
	{
		LOG = LoggerFactory.getLogger(Configuration.class);
		this.watch = new StopWatch("config");
		this.replicators = new HashMap<>();
		this.proxies = new HashMap<>();
		this.coordinators = new HashMap<>();

		try
		{
			watch.start();
			loadConfigurationFile();
		} catch(IOException e)
		{
			LOG.error("failed to load config file");
			e.printStackTrace();
			RuntimeHelper.throwRunTimeException("failed to load config file", ExitCode.XML_ERROR);
		}

		if(!this.checkConfig())
		{
			LOG.error("failed to load config file");
			RuntimeHelper.throwRunTimeException("failed to load config file", ExitCode.XML_ERROR);
		}

		DDLParser parser = new DDLParser(this.schemaFile);
		this.databaseMetadata = parser.parseAnnotations();
		this.watch.stop();

		LOG.info("config file successfull loaded in {} ms", watch.getElapsedTime());
	}

	public static Configuration getInstance()
	{
		return ourInstance;
	}

	private void loadConfigurationFile() throws IOException
	{
		try
		{
			File xmlFile = new File(CONFIG_FILE);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

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

		} catch(Exception e)
		{
			throw new RuntimeException();
		}

		//LOG.info("xml file parsed successfully");
	}

	private void parseVariables(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		this.scratchpadPoolSize = Integer.parseInt(map.getNamedItem("padPoolSize").getNodeValue());
		this.databaseName = map.getNamedItem("dbName").getNodeValue();
		this.schemaFile = map.getNamedItem("schemaFile").getNodeValue();
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
		}
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
		String hostName = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();
		String refReplicator = map.getNamedItem("refReplicator").getNodeValue();
		ReplicatorConfig replicatorConfig = this.getReplicators().get(Integer.parseInt(refReplicator));

		CoordinatorConfig newCoordinator = new CoordinatorConfig(Integer.parseInt(id), hostName, Integer.parseInt
				(port),
				replicatorConfig);

		coordinators.put(Integer.parseInt(id), newCoordinator);
	}

	private void createReplicator(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String hostName = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();
		String dbHost = map.getNamedItem("dbHost").getNodeValue();
		String dbPort = map.getNamedItem("dbPort").getNodeValue();
		String dbUser = map.getNamedItem("dbUser").getNodeValue();
		String dbPwd = map.getNamedItem("dbPwd").getNodeValue();

		ReplicatorConfig newReplicator = new ReplicatorConfig(Integer.parseInt(id), hostName, Integer.parseInt(port),
				dbHost, Integer.parseInt(dbPort), dbUser, dbPwd);

		replicators.put(Integer.parseInt(id), newReplicator);
	}

	private void createProxy(Node n)
	{
		NamedNodeMap map = n.getAttributes();
		String id = map.getNamedItem("id").getNodeValue();
		String hostName = map.getNamedItem("host").getNodeValue();
		String port = map.getNamedItem("port").getNodeValue();
		String dbHost = map.getNamedItem("dbHost").getNodeValue();
		String dbPort = map.getNamedItem("dbPort").getNodeValue();
		String dbUser = map.getNamedItem("dbUser").getNodeValue();
		String dbPwd = map.getNamedItem("dbPwd").getNodeValue();
		String refReplicator = map.getNamedItem("refReplicator").getNodeValue();
		String refCoordinator = map.getNamedItem("refCoordinator").getNodeValue();

		ReplicatorConfig replicatorConfig = this.getReplicators().get(Integer.parseInt(refReplicator));
		CoordinatorConfig coordinatorConfig = this.getCoordinators().get(Integer.parseInt(refCoordinator));

		ProxyConfig newProxy = new ProxyConfig(Integer.parseInt(id), hostName, Integer.parseInt(port), dbHost,
				Integer.parseInt(dbPort), dbUser, dbPwd, replicatorConfig, coordinatorConfig);

		proxies.put(Integer.parseInt(id), newProxy);
	}

	public Map<Integer, ReplicatorConfig> getReplicators()
	{
		return replicators;
	}

	public Map<Integer, ProxyConfig> getProxies()
	{
		return proxies;
	}

	public Map<Integer, CoordinatorConfig> getCoordinators()
	{
		return coordinators;
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public String getSchemaFile()
	{
		return schemaFile;
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

