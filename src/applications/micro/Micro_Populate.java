package applications.micro;


import util.defaults.DBDefaults;

import java.sql.*;
import java.util.Random;


public class Micro_Populate
{

	protected Connection conn;
	protected Statement stat;
	protected String host;
	protected String port;
	protected String url;
	protected String user;
	protected String pwd;
	protected String logicalClockStr;
	final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	Random randomGenerator;
	int recordNum;
	int dcNum;
	int tableNum;

	public Micro_Populate() throws SQLException
	{
		host = DBDefaults.MYSQL_HOST;
		port = DBDefaults.MYSQL_PORT;
		user = DBDefaults.MYSQL_USER;
		pwd = DBDefaults.MYSQL_PASSWORD;
		recordNum = 10;
		dcNum = 1;
		tableNum = 4;
		randomGenerator = new Random();

		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			url = "jdbc:mysql://" + host + ":" + port;

		} catch(Exception e)
		{
			e.printStackTrace();
		}

		init();
	}

	protected void setLogicalClock(int dcNum)
	{
		logicalClockStr = "";
		for(int i = 0; i < dcNum; i++)
		{
			logicalClockStr += "0-";
		}
		logicalClockStr += "0";
	}

	public String get_random_string(int length)
	{
		StringBuilder rndString = new StringBuilder(length);
		for(int i = 0; i < length; i++)
		{
			rndString.append(charSet.charAt(randomGenerator.nextInt(charSet.length())));
		}
		return rndString.toString();
	}

	protected void init() throws SQLException
	{
		conn = DriverManager.getConnection(url, user, pwd);
		conn.setAutoCommit(false);
		stat = conn.createStatement();
		this.setLogicalClock(dcNum);
		this.createDB();
		this.createTables();
		this.insertIntoTables();
	}

	protected void createDB()
	{
		try
		{
			stat.execute("DROP DATABASE IF EXISTS micro");
			conn.commit();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			stat.execute("CREATE DATABASE micro");
		} catch(SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			stat.execute("use micro;");
		} catch(SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			conn.commit();
		} catch(SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void createScratchpadTable() throws SQLException
	{
		stat.execute("use micro");
		stat.execute("CREATE TABLE IF NOT EXISTS SCRATCHPAD_ID ( k int NOT NULL primary key, id int);");
		stat.execute("INSERT INTO SCRATCHPAD_ID VALUES ( 1, 1);");
		stat.execute("CREATE TABLE IF NOT EXISTS SCRATCHPAD_TRX ( k int NOT NULL primary key, id int);");
		stat.execute("INSERT INTO SCRATCHPAD_TRX VALUES ( 1, 1);");
		conn.commit();
	}

	protected void createTables() throws SQLException
	{
		for(int i = 1; i <= tableNum; i++)
		{
			try
			{
				stat.execute("DROP TABLE IF EXISTS t" + i + ";");
			} catch(SQLException e)
			{
				// do nothing
			}
			stat.execute("CREATE TABLE t" + i + " (" +
					"a int(10) NOT NULL," +
					"b int(10) NOT NULL," +
					"c int(10) NOT NULL," +
					"d int(10) unsigned," +
					"e varchar(50)," +
					"_SP_del BIT(1) default false," +
					"_SP_ts int default 0," +
					"_SP_clock varchar(100)," +
					"PRIMARY KEY(a)" +
					");");

		}
		conn.commit();

	}

	protected void insertIntoTables()
	{
		for(int i = 0; i < recordNum; i++)
		{
			int a = i;
			int b = randomGenerator.nextInt(recordNum) + 1;
			int c = randomGenerator.nextInt(recordNum) + 1;
			int d = randomGenerator.nextInt(recordNum) + 1;
			String e = get_random_string(50);
			for(int j = 1; j <= tableNum; j++)
			{
				try
				{
					stat.execute(
							"insert into t" + j + " values (" + Integer.toString(a) + "," + Integer.toString(b) + "," +
									Integer.toString(c) + "," + Integer.toString(
									d) + ",'" + e + "'," + Integer.toString(0) + "," + Integer.toString(
									0) + ",'" + logicalClockStr + "')");
				} catch(SQLException e1)
				{

				}
			}
		}
		try
		{
			conn.commit();
		} catch(SQLException e)
		{
		}
	}
}
