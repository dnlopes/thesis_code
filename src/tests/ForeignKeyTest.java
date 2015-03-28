package tests;


import database.jdbc.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 28/03/15.
 */
public class ForeignKeyTest
{

	public static void main(String args[])
	{
		try
		{
			Connection connection = ConnectionFactory.getDefaultConnection("");
			connection.setAutoCommit(false);
			Statement stat = connection.createStatement();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

}
