package tests;


import database.jdbc.ConnectionFactory;
import org.xml.sax.SAXException;
import util.defaults.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 11/03/15.
 */
public class MicroTest
{

	public static void main(String args[]) throws SQLException, IOException, SAXException, ClassNotFoundException
	{
		//Micro_Populate db = new Micro_Populate();

		Connection conn = ConnectionFactory.getCRDTConnection(Configuration.getInstance().getProxies().get(1));

		Connection conn2 = ConnectionFactory.getDefaultConnection(Configuration.getInstance().getProxies().get(1));

		Statement stat = conn.createStatement();

		int res2 = stat.executeUpdate("update t4 set b=1 where c=10 OR d=10"); // 0,2,5,6
		int res = stat.executeUpdate("update t4 set b=2 where c=10 OR d=10"); // 0,4,8

		//ResultSet rs = stat.executeQuery("SELECT * from t1 where d=9 OR c=10");


		//ResultSet rese = stat.executeQuery("select * from t1");






		//int res = stat.executeUpdate("insert into t1 (a,b,c,d,e) values(54,6,1,1,ZZZZ)");
		conn.commit();
	}

}
