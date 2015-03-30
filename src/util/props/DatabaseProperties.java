package util.props;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by dnlopes on 30/03/15.
 */
public class DatabaseProperties
{

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseProperties.class);

	private String jdbcUrl, jdbcDriver, jdbcUser, jdbcPwd;

	public DatabaseProperties()
	{
		LOG.trace("loading database properties file: database.properties");
		try
		{
			InputStream is = this.getClass().getResourceAsStream("db.properties");
			Properties properties = new Properties();
			properties.load(is);
			if(!this.setup(properties))
				RuntimeHelper.throwRunTimeException("failed to read properties from file", ExitCode.FILENOTFOUND);
		} catch(IOException e)
		{
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.FILENOTFOUND);
		}

	}

	public DatabaseProperties(String propertiesFile)
	{
		LOG.trace("loading database properties file: " + propertiesFile);
		try
		{
			InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(is);
			if(!this.setup(properties))
				RuntimeHelper.throwRunTimeException("failed to read properties from file", ExitCode.FILENOTFOUND);
		} catch(IOException e)
		{
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.FILENOTFOUND);
		}
	}

	private boolean setup(Properties properties)
	{
		try
		{
			this.jdbcUrl = properties.getProperty("db_url");
			this.jdbcDriver = properties.getProperty("db_driver");
			this.jdbcUser = properties.getProperty("db_username");
			this.jdbcPwd = properties.getProperty("db_password");

			return true;
		} catch(Exception e)
		{
			LOG.error("Error while checking database.properties: " + e.getMessage());
			return false;
		}
	}

	public String getJdbcUrl()
	{
		return this.jdbcUrl;
	}

	public String getJdbcDriver()
	{
		return this.jdbcDriver;
	}

	public String getJdbcUser()
	{
		return this.jdbcUser;
	}

	public String getJdbcPwd()
	{
		return this.jdbcPwd;
	}

}