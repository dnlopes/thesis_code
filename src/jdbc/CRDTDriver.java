package jdbc;

import util.Defaults;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by dnlopes on 10/02/15.
 */
public class CRDTDriver implements Driver
{

	static
	{
		try
		{
			// static block to auto-register in the DriverManager
			DriverManager.registerDriver(new CRDTDriver());
		} catch(SQLException E)
		{
			throw new RuntimeException("Error: failed to register CRDT:Driver");
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException
	{
		// verify URL again, because some apps call Driver.getConnection
		// which tries directly to connect and do not check url before
		if(this.acceptsURL(url))
			return new CRDTConnection();

		return null;
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException
	{
		return url.startsWith(Defaults.CRDT_URL_PREFIX);
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
