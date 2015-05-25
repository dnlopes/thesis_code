package database.jdbc;


import nodes.NodeConfig;
import nodes.proxy.Proxy;
import util.defaults.Configuration;
import util.defaults.DBDefaults;

import java.sql.*;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by dnlopes on 10/02/15.
 */
public class CRDTDriver implements Driver
{

	private static final int PROXY_ID = Integer.parseInt(System.getProperty("proxyid"));
	private static final Proxy proxy;

	static
	{
		try
		{
			DriverManager.registerDriver(new CRDTDriver());
			NodeConfig config = Configuration.getInstance().getProxyConfigWithIndex(PROXY_ID);
			proxy = new Proxy(config);

		} catch(SQLException e)
		{
			throw new RuntimeException("Error: failed to register crdt:Driver");
		}
	}

	private CRDTDriver()
	{
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException
	{
		if(this.acceptsURL(url))
			return new CRDTConnection(proxy);

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
