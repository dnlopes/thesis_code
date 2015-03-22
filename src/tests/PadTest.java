package tests;

import database.jdbc.ConnectionFactory;
import database.scratchpad.IDBScratchpad;
import runtime.Configuration;
import database.scratchpad.ExecutePadFactory;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 10/03/15.
 */
public class PadTest
{

	public static void main(String args[]) throws SQLException
	{
		//Micro_Populate db = new Micro_Populate();

		StopWatch watch = new LoggingStopWatch("firstPad");
		watch.start();
		IDBScratchpad pad = ExecutePadFactory.getScratchpad();
		watch.stop();
		watch.setTag("secondPad");
		watch.start();
		IDBScratchpad pad2 = ExecutePadFactory.getScratchpad();
		watch.stop();

		Connection con = ConnectionFactory.getInstance().getCRDTConnection(Configuration.DB_NAME);
		Statement stat = con.createStatement();

		/*
		mysql> select * from t1;
		+---+----+----+----+-----------------+---------+--------+-----------+-----------+
		| a | b  | c  | d  | e               | _SP_del | _SP_ts | _SP_clock | _SP_immut |
		+---+----+----+----+-----------------+---------+--------+-----------+-----------+
		| 0 | 10 | 10 |  3 | qqxoKbLIGfBhdSR |         |      0 | 0-0       |         0 |
		| 1 |  9 |  1 | 10 | gkSJnjFGFbvhFrm |         |      0 | 0-0       |         1 |
		| 2 |  6 |  7 | 10 | LWMEOmhiVRmIPSR |         |      0 | 0-0       |         2 |
		| 3 |  6 |  6 |  8 | RDmngbBXYTstMyx |         |      0 | 0-0       |         3 |
		| 4 |  2 |  2 |  1 | dSptOXtkJnybbzF |         |      0 | 0-0       |         4 |
		| 5 |  1 |  2 |  2 | kJTVHegDlHDVpwq |         |      0 | 0-0       |         5 |
		| 6 |  7 |  2 |  3 | pSsbDbvxRPvcBja |         |      0 | 0-0       |         6 |
		| 7 |  1 |  2 |  2 | FQpJTHpHIdurAgk |         |      0 | 0-0       |         7 |
		| 8 |  3 |  9 |  5 | xFUZcuEbwzXWGtq |         |      0 | 0-0       |         8 |
		| 9 |  6 |  9 |  5 | YzIjgKtkHlxHUjG |         |      0 | 0-0       |         9 |
		+---+----+----+----+-----------------+---------+--------+-----------+-----------+
		*/


		// expected: 4
		StopWatch txn = new LoggingStopWatch("txn");
		txn.start();
		stat.executeUpdate("update t1 set b=10 where d=10 OR c=9");
		txn.stop();
		// expected: 5
		txn.start();
		stat.executeUpdate("delete from t1 where b=10");
		txn.stop();

		//expected: 2
		txn.start();
		ResultSet rs = stat.executeQuery("SELECT * from t1 where d=2");
		txn.stop();
		countResultSetRows(rs);


		// expected: 3
		txn.start();
		rs = stat.executeQuery("SELECT * from t1 where d=2 OR b=3");
		txn.stop();
		countResultSetRows(rs);

		//int res = stat.executeUpdate("insert into t1 (a,b,c,d,e) values(54,6,1,1,ZZZZ)");

	}

	private static int countResultSetRows(ResultSet rs) throws SQLException
	{
		int i = 0;
		while(rs.next())
			i++;
		return i;
	}
}
