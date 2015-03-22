package database.scratchpad;

import org.perf4j.StopWatch;
import util.ObjectPool;
import util.defaults.DBDefaults;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ExecutePadFactory
{

	private static boolean initialized = false;

	private static final Logger LOG = LoggerFactory.getLogger(ExecutePadFactory.class);
	private static ObjectPool<IDBScratchpad> padPool;

	public static void releaseScratchpad(IDBScratchpad sp)
	{
		if(!initialized)
			init();

		padPool.returnObject(sp);
		LOG.trace("Released scratchpad {}", sp.getScratchpadId());
	}

	public static IDBScratchpad getScratchpad()
	{
		if(!initialized)
			init();

		IDBScratchpad sp = padPool.borrowObject();
		LOG.trace("Using scratchpad {}", sp.getScratchpadId());
		return sp;
	}

	private static void init()
	{
		padPool = new ObjectPool<>();

		StopWatch watch = new StopWatch();
		watch.start();
		for(int i = 0; i < DBDefaults.PAD_POOL_SIZE; i++)
		{
			try
			{
				IDBScratchpad scratchpad = new DBExecuteScratchpad(i);
				padPool.addObject(scratchpad);
			} catch(SQLException | ScratchpadException e)
			{
				e.printStackTrace();
			}

		}
		watch.stop();
		LOG.info("{} scratchpads created in {} ms", DBDefaults.PAD_POOL_SIZE, watch.getElapsedTime());
		initialized = true;
	}
}
