package tests;

import applications.micro.Micro_Populate;
import database.jdbc.ConnectionFactory;
import util.defaults.DBDefaults;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 11/03/15.
 */
public class MicroTest
{
	public static void main(String args[]) throws SQLException
	{
		Micro_Populate db = new Micro_Populate();

		//Connection conn = ConnectionFactory.getInstance().getCRDTConnection(DBDefaults.MICRO_DB_NAME);
		//Statement stat = conn.createStatement();

		//ResultSet res = stat.executeQuery("select * from t2");

	}


}
