package util;


import database.jdbc.ConnectionFactory;
import network.AbstractNodeConfig;
import util.defaults.Configuration;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 06/04/15.
 */
public class SetupDatabase
{

	Connection conn;

	public SetupDatabase(Configuration conf) throws SQLException
	{
		//this.conn = ConnectionFactory.getDefaultConnection(config);
	}


	
}
