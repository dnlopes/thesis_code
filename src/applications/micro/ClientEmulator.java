package applications.micro;


import applications.micro.workload.Workload;
import database.jdbc.ConnectionFactory;
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

	public int getSuccessCounterRead()
	{
		return successCounterRead;
	}

	public int getSuccessCounterWrite()
	{
		return successCounterWrite;
	}

	private long sumReadLatency, sumWriteLatency;
	private int successCounterRead, successCounterWrite, abortCounter;

	public ClientEmulator(Workload workload, DatabaseProperties dbProps)
	{
		this.workload = workload;
		this.sumReadLatency = 0;
		this.sumWriteLatency = 0;
		this.successCounterRead = 0;
		this.successCounterWrite = 0;
		this.abortCounter = 0;

		boolean customJDBC = Boolean.parseBoolean(System.getProperty("customJDBC"));

		try
		{
			if(customJDBC)
				this.con = ConnectionFactory.getCRDTConnection(dbProps, "micro");
			else
				this.con = ConnectionFactory.getDefaultConnection(dbProps, "micro");
		} catch(SQLException | ClassNotFoundException e)
		{
			LOG.error("failed to create connection for client: {}", e.getMessage(), e);
			System.exit(-1);
		}
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
				if(Emulator.COUTING)
				{
					if(op.contains("SELECT"))
					{
						this.sumReadLatency += latency;
						this.successCounterRead++;
					} else
					{
						this.sumWriteLatency += latency;
						this.successCounterWrite++;
					}
				}
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
			if(op.contains("SELECT"))
				stat.executeQuery(op);
			else
				stat.executeUpdate(op);

			this.con.commit();
			return true;

		} catch(SQLException e)
		{
			if(LOG.isErrorEnabled())
				LOG.error("op execution error: {}", e.getMessage());
			try
			{
				this.con.rollback();
			} catch(SQLException e1)
			{
				e1.printStackTrace();
			}
			return false;
		}
	}

	public int getAbortCounter()
	{
		return this.abortCounter;
	}

	public float getOperationsAverageReadLatency()
	{
		return this.sumReadLatency / this.successCounterRead;
	}

	public float getOperationsAverageWriteLatency()
	{
		return this.sumWriteLatency / this.successCounterWrite;
	}

	public float getTotalWriteLatency()
	{
		return this.sumWriteLatency;
	}

	public float getTotalReadLatency()
	{
		return this.sumReadLatency;
	}

}
