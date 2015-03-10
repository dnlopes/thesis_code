package database.scratchpad.factory;

import database.scratchpad.DBExecuteScratchpad;
import database.scratchpad.ExecuteScratchpad;
import util.Defaults;
import util.RuntimeExceptions;
import util.debug.Debug;

import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ExecutePadFactory
{

	private Vector<ExecuteScratchpad> queue;
	private ReentrantLock queueLock;
	private Condition condition;

	private static ExecutePadFactory ourInstance = new ExecutePadFactory();

	private ExecutePadFactory()
	{
		this.queueLock = new ReentrantLock();
		this.condition = this.queueLock.newCondition();
		this.queue = new Vector<>(Defaults.PAD_POOL_SIZE);
		try
		{
			this.initialize();
		} catch(SQLException e)
		{
			e.printStackTrace();
			System.exit(RuntimeExceptions.PAD_INIT_FAILED);
		}
		Debug.print("Finished initializing ExecuteScratchpads");
	}

	public static ExecutePadFactory getInstance()
	{
		return ourInstance;
	}

	private void initialize() throws SQLException
	{
		// create pool of pads
		for(int i = 0; i < Defaults.PAD_POOL_SIZE; i++)
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
		return sp;
	}
}
