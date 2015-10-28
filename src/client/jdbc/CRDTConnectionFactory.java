package client.jdbc;


import common.Configuration;
import common.util.DatabaseProperties;
import common.util.RuntimeUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/03/15.
 * This factory creates new connections to the database.
 * A connection can either be a default or a customized connection.
 */
public class CRDTConnectionFactory
{

	public static final String CRDT_DRIVER = "client.jdbc.CRDTDriver";

	static
	{
		try
		{
			Class.forName(CRDT_DRIVER);
		} catch(ClassNotFoundException e)
		{
			RuntimeUtils.throwRunTimeException(e);
		}
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

}
