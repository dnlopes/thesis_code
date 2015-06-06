package applications.micro;


import applications.micro.workload.Workload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.props.DatabaseProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 05/06/15.
 */
public class ClientEmulator extends Thread
{

	private static final Logger LOG = LoggerFactory.getLogger(ClientEmulator.class);

	private Connection con;
	private Workload workload;
	private long sumLatency;
	private int successCounter, abortCounter;

	public ClientEmulator(Workload workload, DatabaseProperties dbProps)
	{
		this.workload = workload;
		this.sumLatency = 0;
		this.successCounter = 0;
		this.abortCounter = 0;
	}

	@Override
	public void run()
	{

		while(Emulator.RUNNING)
		{
			String op = workload.getNextOperation();

			long beginTime = System.nanoTime();
			boolean success = this.doOperation(op);
			long endTime = System.nanoTime();

			long latency = (endTime - beginTime) / 1000000;
			if(success)
			{
				this.sumLatency += latency;
				this.successCounter++;
			} else
				this.abortCounter++;
		}
	}

	private boolean doOperation(String op)
	{
		Statement stat;
		try
		{
			stat = this.con.createStatement();
			stat.execute(op);
			return true;

		} catch(SQLException e)
		{
			if(LOG.isErrorEnabled())
				LOG.error("op execution error: {}", e.getMessage());
			return false;
		}
	}

	public int getAbortCounter()
	{
		return this.abortCounter;
	}

	public int getSuccessCounter()
	{
		return this.successCounter;
	}

	public float getOperationsAverageLatency()
	{
		return this.sumLatency / this.successCounter;
	}
}
