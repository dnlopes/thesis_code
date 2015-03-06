package jdbc;

import util.Defaults;

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
			Class.forName("jdbc.CRDTDriver");
		} catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public Connection getCRDTConnection(String database) throws SQLException
	{
		StringBuilder url = new StringBuilder(Defaults.CRDT_URL_PREFIX);
		url.append(database);

		return DriverManager.getConnection(url.toString(), Defaults.MYSQL_USER, Defaults.MYSQL_PASSWORD);
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
		StringBuilder url = new StringBuilder(Defaults.CRDT_URL_PREFIX);
		url.append(database);

		return DriverManager.getConnection(url.toString(), user, password);
	}

	public Connection getDefaultConnection(String database) throws SQLException
	{
		StringBuilder url = new StringBuilder(Defaults.DEFAULT_URL_PREFIX);
		url.append(database);

		return DriverManager.getConnection(url.toString(), Defaults.MYSQL_USER, Defaults.MYSQL_PASSWORD);
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
		StringBuilder url = new StringBuilder(Defaults.DEFAULT_URL_PREFIX);
		url.append(database);

		return DriverManager.getConnection(url.toString(), user, password);
	}


}
