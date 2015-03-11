package tests;

import database.scratchpad.ExecuteScratchpad;
import database.scratchpad.ExecutePadFactory;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public class PadTest
{

	public static void main(String args[]) throws SQLException
	{
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
