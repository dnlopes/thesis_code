package database.jdbc;


import network.proxy.Proxy;
import org.apache.thrift.transport.TTransportException;
import util.defaults.Configuration;
import util.defaults.DBDefaults;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by dnlopes on 10/02/15.
 */
public class CRDTDriver implements Driver
{

	private static final int THIS_PROXY_ID = 1;

	static
	{
		try
		{
			DriverManager.registerDriver(new CRDTDriver());
			Configuration.PROXY = new Proxy(Configuration.getInstance().getProxies().get(THIS_PROXY_ID));

		} catch(SQLException E)
		{
			throw new RuntimeException("Error: failed to register CRDT:Driver");
		} catch(TTransportException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException
	{
		// verify URL again, because some apps call Driver.getConnection
		// which tries directly to connect and do not check url before
		if(this.acceptsURL(url))
			return new CRDTConnection(Configuration.PROXY);

		return null;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException
	{
		return url.startsWith(DBDefaults.CRDT_URL_PREFIX);
	}


	/* Stubs */

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
	{
		return new DriverPropertyInfo[0];
	}

	@Override
	public int getMajorVersion()
	{
		return 0;
	}

	@Override
	public int getMinorVersion()
	{
		return 0;
	}

	@Override
	public boolean jdbcCompliant()
	{
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		return null;
	}
}
