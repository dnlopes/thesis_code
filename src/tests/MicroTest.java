package tests;

import applications.micro.Micro_Populate;
import database.jdbc.ConnectionFactory;
import runtime.Configuration;

import java.sql.Connection;
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

		Connection conn = ConnectionFactory.getInstance().getCRDTConnection(Configuration.DB_NAME);

		Statement stat = conn.createStatement();

		//ResultSet rese = stat.executeQuery("select * from t1");

		//int res = stat.executeUpdate("update t1 set c=15 where d=1");
		int res = stat.executeUpdate("insert into t1 (a,b,c,d,e) values(50,1,1,1,1)");
		conn.commit();
	}


}
