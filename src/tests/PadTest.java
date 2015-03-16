package tests;

import applications.micro.Micro_Populate;
import database.jdbc.ConnectionFactory;
import database.occ.scratchpad.IDBScratchpad;
import runtime.Configuration;
import database.occ.scratchpad.ExecutePadFactory;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

import java.sql.Connection;
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
		stat.executeUpdate("update address set addr_state='ola' where addr_state='cenas';");
	}
}
