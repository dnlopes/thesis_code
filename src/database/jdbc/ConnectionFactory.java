package database.jdbc;

import util.Defaults;
import util.debug.Debug;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/03/15.
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
		return this.getCRDTConnection(database, Defaults.MYSQL_USER, Defaults.MYSQL_PASSWORD);
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
		StringBuilder url = new StringBuilder(Defaults.CRDT_URL);
		url.append(database);

		return DriverManager.getConnection(url.toString(), user, password);
	}

	public Connection getDefaultConnection(String database) throws SQLException
	{
		return this.getDefaultConnection(database, Defaults.MYSQL_USER, Defaults.MYSQL_PASSWORD);
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
		StringBuilder url = new StringBuilder(Defaults.DEFAULT_URL);
		url.append(database);
		return DriverManager.getConnection(url.toString(), user, password);
	}


}
