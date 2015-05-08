package database.jdbc;


import network.AbstractNodeConfig;
import util.defaults.Configuration;
import util.defaults.DBDefaults;
import util.props.DatabaseProperties;

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

	private static final String CRDT_DRIVER = "database.jdbc.CRDTDriver";
	private static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";

	static
	{
		try
		{
			Class.forName(CRDT_DRIVER);
			Class.forName(DEFAULT_DRIVER);
		} catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static Connection getDefaultConnection(DatabaseProperties props, String databaseName)
			throws SQLException, ClassNotFoundException
	{
		StringBuffer buffer = new StringBuffer(DBDefaults.DEFAULT_URL_PREFIX);
		buffer.append(props.getDbHost());
		buffer.append(":");
		buffer.append(props.getDbPort());
		buffer.append("/");
		buffer.append(databaseName);

		Connection c = DriverManager.getConnection(buffer.toString(), props.getDbUser(), props.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(DatabaseProperties props) throws SQLException, ClassNotFoundException
	{
		return getDefaultConnection(props, "");
	}

	public static Connection getCRDTConnection(DatabaseProperties props, String databaseName)
			throws SQLException, ClassNotFoundException
	{
		StringBuffer buffer = new StringBuffer(DBDefaults.CRDT_URL_PREFIX);
		buffer.append(props.getDbHost());
		buffer.append(":");
		buffer.append(props.getDbPort());
		buffer.append("/");
		buffer.append(databaseName);

		Connection c = DriverManager.getConnection(buffer.toString(), props.getDbUser(), props.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(AbstractNodeConfig nodeInfo) throws SQLException
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

	public static Connection getCRDTConnection(AbstractNodeConfig nodeInfo) throws SQLException
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
}
