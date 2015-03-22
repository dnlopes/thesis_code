package database.jdbc;


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

	/**
	 * @param database
	 * 		host:port
	 * @param user
	 * @param password
	 *
	 * @return a new connection to the database
	 *
	 * @throws SQLException
	 */
	public static Connection getCRDTConnection(String database, String user, String password) throws SQLException
	{
		StringBuilder url = new StringBuilder(DBDefaults.CRDT_URL);
		url.append(database);
		Connection c = DriverManager.getConnection(url.toString(), user, password);
		c.setAutoCommit(false);

		return c;
	}

	public static Connection getDefaultConnection(String database) throws SQLException
	{
		return getDefaultConnection(database, DBDefaults.MYSQL_USER, DBDefaults.MYSQL_PASSWORD);
	}

	/**
	 * @param database
	 * 		host:port
	 * @param user
	 * @param password
	 *
	 * @return a new connection to the database
	 *
	 * @throws SQLException
	 */
	public static Connection getDefaultConnection(String database, String user, String password) throws SQLException
	{
		StringBuilder url = new StringBuilder(DBDefaults.DEFAULT_URL);
		url.append(database);
		Connection c = DriverManager.getConnection(url.toString(), user, password);
		c.setAutoCommit(false);

		return c;
	}

}
