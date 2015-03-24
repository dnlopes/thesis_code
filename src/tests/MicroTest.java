package tests;


import applications.micro.Micro_Populate;
import database.jdbc.ConnectionFactory;
import org.xml.sax.SAXException;
import util.defaults.Configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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

		Statement stat = conn.createStatement();


		int res = stat.executeUpdate("update t1 set b=9 where a=8");
		res = stat.executeUpdate("update t1 set b=7 where a=4");
		res = stat.executeUpdate("update t1 set c=20 where d>=7");
		conn.commit();


		//alterou o ID=9 (b=15)
		//int res2 = stat.executeUpdate("update t1 set b=10 where a=2");
		// alterou o ID=2 (b=10)
		//res = stat.executeUpdate("update t1 set c=6 where a=5 AND b=15");


		 /*




		//int res = stat.executeUpdate("insert into t1 (a,b,c,d,e) values(54,6,1,1,ZZZZ)");
		conn.commit();
		stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT * from t1 where d=9 OR c=10");


		rs = stat.executeQuery("SELECT * from t1 where d=9 OR c=10");
		conn.commit();
		rs = stat.executeQuery("select * from t1 where a>0");
		res = stat.executeUpdate("update t3 set b=2 where c>2 OR d=10"); // 0,4,8
		*/

	}

}
