package database.jdbc;


import nodes.NodeConfig;
import util.Configuration;
import util.defaults.DatabaseDefaults;
import util.DatabaseProperties;

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

	public static Connection getDefaultConnection(DatabaseProperties props, String databaseName) throws SQLException
	{
		StringBuffer buffer = new StringBuffer(DatabaseDefaults.DEFAULT_URL_PREFIX);
		buffer.append(props.getDbHost());
		buffer.append(":");
		buffer.append(props.getDbPort());
		buffer.append("/");
		buffer.append(databaseName);

		Connection c = DriverManager.getConnection(buffer.toString(), props.getDbUser(), props.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getCRDTConnection(DatabaseProperties props, String databaseName)
			throws SQLException, ClassNotFoundException
	{
		StringBuffer buffer = new StringBuffer(CRDTDriver.CRDT_URL_PREFIX);
		buffer.append(props.getDbHost());
		buffer.append(":");
		buffer.append(props.getDbPort());
		buffer.append("/");
		buffer.append(databaseName);

		if(Configuration.getInstance().optimizeBatch())
			buffer.append("?rewriteBatchedStatements=true");

		Connection c = DriverManager.getConnection(buffer.toString(), props.getDbUser(), props.getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(NodeConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(DatabaseDefaults.DEFAULT_URL_PREFIX);
		url.append(nodeInfo.getDbProps().getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbProps().getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbProps().getDbUser(),
				nodeInfo.getDbProps().getDbPwd());
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getCRDTConnection(NodeConfig nodeInfo) throws SQLException
	{
		StringBuffer url = new StringBuffer(CRDTDriver.CRDT_URL_PREFIX);
		url.append(nodeInfo.getDbProps().getDbHost());
		url.append(":");
		url.append(nodeInfo.getDbProps().getDbPort());
		url.append("/");
		url.append(Configuration.getInstance().getDatabaseName());

		if(Configuration.getInstance().optimizeBatch())
			url.append("?rewriteBatchedStatements=true");

		Connection c = DriverManager.getConnection(url.toString(), nodeInfo.getDbProps().getDbUser(),
				nodeInfo.getDbProps().getDbPwd());
		c.setAutoCommit(false);

		return c;
	}
}
