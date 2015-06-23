package tests;


import database.jdbc.ConnectionFactory;
import util.DatabaseProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 19/06/15.
 */
public class SQLTester
{
	private final static String DB_HOST = "172.16.24.224";

	public static void main(String[] args) throws SQLException, ClassNotFoundException
	{
		DatabaseProperties props = new DatabaseProperties("sa", "101010", DB_HOST, 3306);

		System.setProperty("proxyid", "1");
		System.setProperty("usersNum", "1");
		System.setProperty("configPath",
				"/Users/dnlopes/devel/thesis/code/weakdb/resources/configs/micro_localhost_1node.xml");


		Connection conn = ConnectionFactory.getCRDTConnection(props, "micro");
		Statement stat = conn.createStatement();


		// delete parent
		//stat.executeUpdate("DELETE FROM t1 where a=5");

		// insert child
		//stat.executeUpdate("INSERT INTO t2 (a,b,c,d,e) VALUES (122341,500,10,10,'CENAS')");

		// simple update
		//stat.executeUpdate("UPDATE t2 set e='COCO' where a=500");

		// update parent
		stat.executeUpdate("UPDATE t1 set b='66666' where a=100");

		conn.commit();
	}
}
