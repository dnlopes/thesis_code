package tests;

import database.parser.DDLParser;
import database.scratchpad.ExecuteScratchpad;
import database.scratchpad.ExecutePadFactory;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import util.defaults.DBDefaults;

import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public class PadTest
{

	public static void main(String args[]) throws SQLException
	{
		DDLParser parser = new DDLParser(DBDefaults.TPCW_FILE);
		parser.parseAnnotations();

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
