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
	private static ConnectionFactory ourInstance = new ConnectionFactory();

	public static ConnectionFactory getInstance()
	{
		return ourInstance;
	}

	private ConnectionFactory()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("database.jdbc.CRDTDriver");
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public Connection getCRDTConnection(String database) throws SQLException
	{
		return this.getCRDTConnection(database, DBDefaults.MYSQL_USER, DBDefaults.MYSQL_PASSWORD);
	}

	/**
	 * @param database host:port
	 * @param user
	 * @param password
	 *
	 * @return a new connection to the database
	 *
	 * @throws SQLException
	 */
	public Connection getCRDTConnection(String database, String user, String password) throws SQLException
	{
		StringBuilder url = new StringBuilder(DBDefaults.CRDT_URL);
		url.append(database);

        Connection c = DriverManager.getConnection(url.toString(), user, password);
        c.setAutoCommit(false);
        return c;
	}

	public Connection getDefaultConnection(String database) throws SQLException
	{
		return this.getDefaultConnection(database, DBDefaults.MYSQL_USER, DBDefaults.MYSQL_PASSWORD);
	}

	/**
	 * @param database host:port
	 * @param user
	 * @param password
	 *
	 * @return a new connection to the database
	 *
	 * @throws SQLException
	 */
	public Connection getDefaultConnection(String database, String user, String password) throws SQLException
	{
		StringBuilder url = new StringBuilder(DBDefaults.DEFAULT_URL);
		url.append(database);
        Connection c = DriverManager.getConnection(url.toString(), user, password);
        c.setAutoCommit(false);
        return c;
	}


}
