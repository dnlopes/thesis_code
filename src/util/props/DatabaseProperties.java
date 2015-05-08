package util.props;


import network.AbstractNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.DBDefaults;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by dnlopes on 30/03/15.
 */
public class DatabaseProperties
{

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseProperties.class);

	private String dbUser;
	private String dbPwd;
	private String dbHost;
	private int dbPort;

	public DatabaseProperties (String user, String pwd, String host, int port)
	{
		this.dbHost = host;
		this.dbPort = port;
		this.dbUser = user;
		this.dbPwd = pwd;
	}
	public DatabaseProperties(String propertiesFile, boolean inClassPath)
	{
		LOG.trace("loading database properties file: " + propertiesFile);
		try
		{
			InputStream inputStream;

			if(inClassPath)
				inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
			else
				inputStream = new FileInputStream(propertiesFile);

			Properties properties = new Properties();
			properties.load(inputStream);
			if(!this.setup(properties))
				RuntimeHelper.throwRunTimeException("failed to read properties from file", ExitCode.FILENOTFOUND);
		} catch(IOException e)
		{
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.FILENOTFOUND);
		}
	}

	public DatabaseProperties(AbstractNodeConfig config)
	{
		this.dbHost = config.getHostName();
		this.dbPort = config.getDbPort();
		this.dbUser = config.getDbUser();
		this.dbPwd = config.getDbPwd();
	}

	private boolean setup(Properties properties)
	{
		try
		{
			this.dbHost = properties.getProperty("db_host");
			this.dbPort = Integer.parseInt(properties.getProperty("db_port"));
			this.dbUser = properties.getProperty("db_username");
			this.dbPwd = properties.getProperty("db_password");

			return true;
		} catch(Exception e)
		{
			LOG.error("Error while checking database.properties: " + e.getMessage());
			return false;
		}
	}

	public String getDbUser()
	{
		return this.dbUser;
	}

	public String getDbPwd()
	{
		return this.dbPwd;
	}

	public String getDbHost()
	{
		return dbHost;
	}

	public int getDbPort()
	{
		return dbPort;
	}

	public String getUrl()
	{
		StringBuffer buffer = new StringBuffer(DBDefaults.DEFAULT_URL_PREFIX);
		buffer.append(this.getDbHost());
		buffer.append(":");
		buffer.append(this.getDbPort());
		buffer.append("/");

		return buffer.toString();
	}

}
