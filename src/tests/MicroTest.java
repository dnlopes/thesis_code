package tests;


import applications.micro.Micro_Populate;
import database.jdbc.ConnectionFactory;
import database.jdbc.Result;
import network.proxy.ProxyConfig;
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


	public static void main(String args[]) throws IOException, ClassNotFoundException
	{
		//Micro_Populate db = new Micro_Populate();

		Connection conn = null;
		Statement stat = null;
		try
		{
			conn = ConnectionFactory.getCRDTConnection();

			stat = conn.createStatement();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		int res;
		ResultSet rs;
		try
		{

			//res = stat.executeUpdate("update t2 set d=20 where a=1");
			res = stat.executeUpdate("insert into t1 (a,b,d,e) values(55,6,1,'OLA')");
			conn.commit();
			System.exit(1);
			rs = stat.executeQuery("SELECT * from t2 where a=5");

			res = stat.executeUpdate("insert into t1 (a,b,d,e) values(54,6,1,'OLA')");
			res = stat.executeUpdate("update t2 set d=21 where a=2");
			//res = stat.executeUpdate("update t1 set b=20 where a=3");



		} catch(SQLException e)
		{
			e.printStackTrace();
		}



		try
		{
			res = stat.executeUpdate("update t4 set d=20 where a=1");
			rs = stat.executeQuery("SELECT * from t3 where a=5");

			//res = stat.executeUpdate("insert into t2 (a,b,d,e) values(54,6,1,'OLA')");
			res = stat.executeUpdate("update t2 set d=21 where a=2");
			res = stat.executeUpdate("update t2 set b=20 where a=3");
			conn.commit();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}

/*
		res = stat.executeUpdate("update t2 set c=643 where a=4");
		conn.commit();

		res = stat.executeUpdate("update t3 set c=20 where d>=7");
		rs = stat.executeQuery("SELECT * from t3 where d=9 OR c=10");
		res = stat.executeUpdate("update t3 set c=643 where a=4");
		conn.commit();

/*
		conn = ConnectionFactory.getCRDTConnection(Configuration.getInstance().getProxies().get(1));

		stat = conn.createStatement();

		conn.commit();
		res = stat.executeUpdate("update t1 set c=283 where a=2");
		//res = stat.executeUpdate("select a,b from t2 where a>0");
		res = stat.executeUpdate("update t1 set c=9199 where a=1");
		conn.commit();

		//int res = stat.executeUpdate("update t1 set b=22 where a>4");
		//res = stat.executeUpdate("update t1 set c=222 where d<=7");
		//conn.commit();
		//res = stat.executeUpdate("update t3 set b=22 where a>4");
		//res = stat.executeUpdate("update t3 set c=222 where d<=7 OR c>1");
		//rs = stat.executeQuery("SELECT a from t1 where d=9 OR c=10");

		//conn.commit();

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
