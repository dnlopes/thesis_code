package database.scratchpad;


import org.perf4j.StopWatch;
import util.defaults.Configuration;
import util.ObjectPool;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ScratchpadFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(ScratchpadFactory.class);
	private static ScratchpadFactory ourInstance = new ScratchpadFactory();

	private ObjectPool<IDBScratchpad> padPool;

	private ScratchpadFactory()
	{
		setupScratchpads();
	}

	public static ScratchpadFactory getInstante()
	{
		return ourInstance;
	}

	public void releaseScratchpad(IDBScratchpad sp)
	{
		this.padPool.returnObject(sp);
		LOG.trace("Released scratchpad {}", sp.getScratchpadId());
	}

	public IDBScratchpad getScratchpad()
	{
		IDBScratchpad sp = padPool.borrowObject();
		LOG.trace("Using scratchpad {}", sp.getScratchpadId());
		return sp;
	}

	private void setupScratchpads()
	{
		this.padPool = new ObjectPool<>();

		StopWatch watch = new StopWatch();
		watch.start();
		for(int i = 0; i < Configuration.getInstance().getScratchpadPoolSize(); i++)
		{
			try
			{
				IDBScratchpad scratchpad = new DBExecuteScratchpad(i);
				this.padPool.addObject(scratchpad);
			} catch(SQLException | ScratchpadException e)
			{
				e.printStackTrace();
			}

		}
		watch.stop();
		LOG.info("{} scratchpads created in {} ms", Configuration.getInstance().getScratchpadPoolSize(),
				watch.getElapsedTime());
	}
}
