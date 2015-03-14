package tests;

import database.jdbc.ConnectionFactory;
import database.parser.DDLParser;
import database.scratchpad.ExecuteScratchpad;
import database.scratchpad.ExecutePadFactory;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import util.defaults.DBDefaults;

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
		//DDLParser parser = new DDLParser(DBDefaults.TPCW_FILE);
		//parser.parseAnnotations();

		Connection con = ConnectionFactory.getInstance().getCRDTConnection("tpcw");

		Statement stat = con.createStatement();
		stat.executeUpdate("update address set addr_state='ola' where addr_state='cenas';");

		StopWatch watch = new LoggingStopWatch("firstPad");
		watch.start();
		ExecuteScratchpad pad = ExecutePadFactory.getInstance().getScratchpad();
		watch.stop();
		watch.setTag("secondPad");
		watch.start();
		ExecuteScratchpad pad2 = ExecutePadFactory.getInstance().getScratchpad();
		watch.stop();
	}
}
