package database.scratchpad;

import util.ExitCode;
import util.defaults.DBDefaults;

import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ExecutePadFactory
{

	static final Logger LOG = LoggerFactory.getLogger(ExecutePadFactory.class);

	private Vector<ExecuteScratchpad> queue;
	private ReentrantLock queueLock;
	private Condition condition;

	private static ExecutePadFactory ourInstance = new ExecutePadFactory();

	private ExecutePadFactory()
	{
		this.queueLock = new ReentrantLock();
		this.condition = this.queueLock.newCondition();
		this.queue = new Vector<>(DBDefaults.PAD_POOL_SIZE);
		try
		{
			this.initialize();
			LOG.info("Scratchpads created");

		} catch(SQLException | ScratchpadException e)
		{
			e.printStackTrace();
			System.exit(ExitCode.SCRATCHPAD_INIT_FAILED);
		}
	}

	public static ExecutePadFactory getInstance()
	{
		return ourInstance;
	}

	private void initialize() throws SQLException, ScratchpadException
	{
		// create pool of pads
		for(int i = 0; i < DBDefaults.PAD_POOL_SIZE; i++)
		{
			ExecuteScratchpad pad = new DBExecuteScratchpad(i);
			this.queue.add(pad);
		}
	}

	public void releaseScratchpad(ExecuteScratchpad sp)
	{
		this.queueLock.lock();
		try
		{
			this.queue.add(sp);
			this.condition.signal();
		} finally
		{
			this.queueLock.unlock();
		}
		LOG.info("Released scratchpad {}", sp.getScratchpadId());
	}

	public ExecuteScratchpad getScratchpad()
	{
		ExecuteScratchpad sp = null;
		this.queueLock.lock();
		try
		{
			while(this.queue.isEmpty())
			{
				try
				{
					this.condition.await();
				} catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			sp = this.queue.remove(0);
		} finally
		{
			this.queueLock.unlock();
		}
		LOG.info("Using scratchpad {}", sp.getScratchpadId());
		return sp;
	}
}
