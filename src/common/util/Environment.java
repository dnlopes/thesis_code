package common.util;


import common.database.constraints.Constraint;
import common.database.table.DatabaseTable;
import common.database.util.DatabaseMetadata;
import common.parser.DDLParser;
import common.util.exception.ConfigurationLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.agents.AgentsFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Created by dnlopes on 28/10/15.
 */
public class Environment
{

	private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

	private volatile static boolean IS_CONFIGURED = false;
	private static Environment instance;

	public static boolean IS_ZOOKEEPER_REQUIRED = false;
	public static String ENVIRONMENT_FILE;
	public static String DDL_ANNOTATIONS_FILE;
	public static int EZK_CLIENTS_POOL_SIZE;
	public static int REPLICATORS_CONNECTIONS_POOL_SIZE;
	public static int COMMIT_PAD_POOL_SIZE;
	public static boolean OPTIMIZE_BATCH;
	public static String EZK_EXTENSION_CODE;
	public static String DATABASE_NAME;
	public static DatabaseMetadata DB_METADATA;
	public static int DISPATCHER_AGENT;
	public static int DELIVER_AGENT;

	private Environment(String envFile) throws ConfigurationLoadException
	{
		if(envFile == null)
			throw new ConfigurationLoadException("environment file is null");

		ENVIRONMENT_FILE = envFile;

		loadConfigurations(true);
		loadAnnotationsFile();

		IS_CONFIGURED = true;
		//printEnvironment();
	}

	public static Environment getInstance()
	{
		return instance;
	}

	private static void printEnvironment()
	{
		LOG.info("environment:" + EnvironmentDefaults.DATABASE_NAME_VAR + "=" + Environment.DATABASE_NAME);
		LOG.info("environment:" + EnvironmentDefaults.DDL_FILE_VAR + "=" + Environment.DDL_ANNOTATIONS_FILE);
		LOG.info(
				"environment:" + EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR + "=" + Environment
						.COMMIT_PAD_POOL_SIZE);
		LOG.info("environment:" + EnvironmentDefaults.EZK_EXTENSION_CODE_VAR + "=" + Environment.EZK_EXTENSION_CODE);
		LOG.info(
				"environment:" + EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR + "=" + Environment
						.EZK_CLIENTS_POOL_SIZE);
		LOG.info("environment:" + EnvironmentDefaults.OPTIMIZE_BATCH_VAR + "=" + Environment.OPTIMIZE_BATCH);
		LOG.info(
				"environment:" + EnvironmentDefaults.DELIVER_NAME_VAR + "=" + AgentsFactory
						.getDeliverAgentClassAsString());
		LOG.info(
				"environment:" + EnvironmentDefaults.DISPATCHER_NAME_VAR + "=" + AgentsFactory
						.getDispatcherAgentClassAsString());
	}

	public static synchronized void setupEnvironment(String envFile) throws ConfigurationLoadException
	{
		if(IS_CONFIGURED)
			LOG.warn("environment configuration already loaded");
		else
			instance = new Environment(envFile);

	}

	private void loadConfigurations(boolean lookForAnnotationsFile) throws ConfigurationLoadException
	{
		if(ENVIRONMENT_FILE == null)
			throw new ConfigurationLoadException("environment file not set");

		Properties prop = new Properties();

		try
		{
			prop.load(new FileInputStream(ENVIRONMENT_FILE));

			if(prop.containsKey(Environment.EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR))
				Environment.COMMIT_PAD_POOL_SIZE = Integer.parseInt(
						prop.getProperty(Environment.EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_VAR));
			else
				Environment.COMMIT_PAD_POOL_SIZE = Environment.EnvironmentDefaults.COMMIT_PAD_POOL_SIZE_DEFAULT;
			if(prop.containsKey(Environment.EnvironmentDefaults.REPLICATORS_CONNECTIONS_POOL_SIZE_VAR))
				Environment.COMMIT_PAD_POOL_SIZE = Integer.parseInt(
						prop.getProperty(Environment.EnvironmentDefaults.REPLICATORS_CONNECTIONS_POOL_SIZE_VAR));
			else
				Environment.REPLICATORS_CONNECTIONS_POOL_SIZE = EnvironmentDefaults.REPLICATORS_CONNECTIONS_POOL_SIZE_DEFAULT;

			if(prop.containsKey(Environment.EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR))
				Environment.EZK_CLIENTS_POOL_SIZE = Integer.parseInt(
						prop.getProperty(Environment.EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_VAR));
			else
				Environment.EZK_CLIENTS_POOL_SIZE = Environment.EnvironmentDefaults.EZK_CLIENTS_POOL_SIZE_DEFAULT;

			if(prop.containsKey(Environment.EnvironmentDefaults.OPTIMIZE_BATCH_VAR))
				Environment.OPTIMIZE_BATCH = Boolean.parseBoolean(
						prop.getProperty(Environment.EnvironmentDefaults.OPTIMIZE_BATCH_VAR));
			else
				Environment.OPTIMIZE_BATCH = Environment.EnvironmentDefaults.OPTIMIZE_BATCH_DEFAULT;

			if(prop.containsKey(Environment.EnvironmentDefaults.DISPATCHER_NAME_VAR))
				Environment.DISPATCHER_AGENT = Integer.parseInt(
						prop.getProperty(Environment.EnvironmentDefaults.DISPATCHER_NAME_VAR));
			else
				Environment.DISPATCHER_AGENT = Environment.EnvironmentDefaults.DISPATCHER_AGENT_DEFAULT;

			if(prop.containsKey(Environment.EnvironmentDefaults.DELIVER_NAME_VAR))
				Environment.DELIVER_AGENT = Integer.parseInt(
						prop.getProperty(Environment.EnvironmentDefaults.DELIVER_NAME_VAR));
			else
				Environment.DELIVER_AGENT = Environment.EnvironmentDefaults.DELIVER_AGENT_DEFAULT;

			if(prop.containsKey(Environment.EnvironmentDefaults.DATABASE_NAME_VAR))
				Environment.DATABASE_NAME = prop.getProperty(Environment.EnvironmentDefaults.DATABASE_NAME_VAR);
			else
				throw new ConfigurationLoadException("missing mandatory 'dbname' parameter in environment file");
			if(prop.containsKey(Environment.EnvironmentDefaults.EZK_EXTENSION_CODE_VAR))
				Environment.EZK_EXTENSION_CODE = prop.getProperty(
						Environment.EnvironmentDefaults.EZK_EXTENSION_CODE_VAR);
			else
				throw new ConfigurationLoadException("missing mandatory 'ezk-extension-code-dir' in environment file");

			if(lookForAnnotationsFile)
			{
				if(prop.containsKey(EnvironmentDefaults.DDL_FILE_VAR))
					Environment.DDL_ANNOTATIONS_FILE = prop.getProperty(Environment.EnvironmentDefaults.DDL_FILE_VAR);
				else
					throw new ConfigurationLoadException("missing mandatory 'ddl-file' in environment file");
			}

		} catch(IOException e)
		{
			throw new ConfigurationLoadException(e.getMessage());
		}
	}

	private void loadAnnotationsFile() throws ConfigurationLoadException
	{
		if(DDL_ANNOTATIONS_FILE == null)
			throw new ConfigurationLoadException("ddl annotations file not set");

		DDLParser parser = new DDLParser(DDL_ANNOTATIONS_FILE);
		DB_METADATA = parser.parseAnnotations();

		LOG.trace("ddl annotations file loaded: {}", DDL_ANNOTATIONS_FILE);

		for(DatabaseTable table : DB_METADATA.getAllTables())
		{
			for(Constraint c : table.getUniqueConstraints())
			{
				if(c.requiresCoordination())
				{
					IS_ZOOKEEPER_REQUIRED = true;
					return;
				}
			}
			for(Constraint c : table.getAutoIncrementConstraints())
			{
				if(c.requiresCoordination())
				{
					IS_ZOOKEEPER_REQUIRED = true;
					return;
				}
			}
			for(Constraint c : table.getCheckConstraints())
			{
				//TODO
				// after proper implementation of check constraints
				// uncomment the next block
				/*
				if(c.requiresCoordination())
				{
					IS_ZOOKEEPER_REQUIRED = true;
					return;
				}           */
			}
		}
	}

	public interface EnvironmentDefaults
	{

		int EZK_CLIENTS_POOL_SIZE_DEFAULT = 20;
		int REPLICATORS_CONNECTIONS_POOL_SIZE_DEFAULT = 50;
		int COMMIT_PAD_POOL_SIZE_DEFAULT = 50;
		boolean OPTIMIZE_BATCH_DEFAULT = false;
		int DISPATCHER_AGENT_DEFAULT = 2;
		int DELIVER_AGENT_DEFAULT = 1;

		String DDL_FILE_VAR = "ddl-file";
		String REPLICATORS_CONNECTIONS_POOL_SIZE_VAR = "replicators-con-pool-size";
		String EZK_CLIENTS_POOL_SIZE_VAR = "ezk-client-pool-size";
		String COMMIT_PAD_POOL_SIZE_VAR = "commit-pool-size";
		String OPTIMIZE_BATCH_VAR = "optimize-batch";
		String EZK_EXTENSION_CODE_VAR = "ezk-extension-code-dir";
		String DATABASE_NAME_VAR = "dbname";
		String DISPATCHER_NAME_VAR = "dispatcher";
		String DELIVER_NAME_VAR = "deliver";
	}
}
