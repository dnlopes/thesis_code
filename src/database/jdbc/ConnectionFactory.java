package database.jdbc;


import network.proxy.ProxyConfig;
import network.replicator.ReplicatorConfig;
import util.defaults.Configuration;
import util.defaults.DBDefaults;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/03/15.
 * This factory creates new connections to the database.
 * A connection can either be a default or a customized connection.
 */
public class ConnectionFactory
{

	static
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("database.jdbc.CRDTDriver");
		} catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static Connection getCRDTConnection(String database) throws SQLException
	{
		return getCRDTConnection(database, DBDefaults.MYSQL_USER, DBDefaults.MYSQL_PASSWORD);
	}

	public static Connection getCRDTConnection() throws SQLException
	{
		return getCRDTConnection(CRDTConnection.THIS_PROXY.getConfig());
	}

	public static Connection getCRDTConnection(ProxyConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(DBDefaults.CRDT_URL_PREFIX);
		url.append(nodeInfo.getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbUser(), nodeInfo.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getCRDTConnection(ReplicatorConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(DBDefaults.CRDT_URL_PREFIX);
		url.append(nodeInfo.getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbUser(), nodeInfo.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	private static Connection getCRDTConnection(String database, String user, String password) throws SQLException
	{
		Connection c = DriverManager.getConnection(database, user, password);
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(String database) throws SQLException
	{
		return getDefaultConnection(database, DBDefaults.MYSQL_USER, DBDefaults.MYSQL_PASSWORD);
	}

	public static Connection getDefaultConnection(ProxyConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(DBDefaults.DEFAULT_URL_PREFIX);
		url.append(nodeInfo.getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbUser(), nodeInfo.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(ReplicatorConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(DBDefaults.DEFAULT_URL_PREFIX);
		url.append(nodeInfo.getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbUser(), nodeInfo.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	private static Connection getDefaultConnection(String database, String user, String password) throws SQLException
	{
		StringBuilder url = new StringBuilder(DBDefaults.DEFAULT_URL);
		url.append(database);
		Connection c = DriverManager.getConnection(url.toString(), user, password);
		c.setAutoCommit(false);

		return c;
	}

}
