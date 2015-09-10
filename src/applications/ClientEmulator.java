package applications;


import database.jdbc.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/06/15.
 */
public class ClientEmulator implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(ClientEmulator.class);

	private Connection connection;
	private final BenchmarkOptions options;
	private long sumReadLatency, sumWriteLatency;
	private int successCounterRead, successCounterWrite, abortCounter;

	public ClientEmulator(BenchmarkOptions options)
	{
		this.options = options;
		this.sumReadLatency = 0;
		this.sumWriteLatency = 0;
		this.successCounterRead = 0;
		this.successCounterWrite = 0;
		this.abortCounter = 0;

		try
		{
			if(this.options.isCRDTDriver())
				this.connection = ConnectionFactory.getCRDTConnection(this.options.getDbProps(),
						this.options.getDatabaseName());
			else
				this.connection = ConnectionFactory.getDefaultConnection(this.options.getDbProps(),
						this.options.getDatabaseName());
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
			Transaction txn = this.options.getWorkload().getNextTransaction();

			long beginTime = System.nanoTime();
			boolean success = txn.executeTransaction(this.connection);
			long endTime = System.nanoTime();

			long latency = (endTime - beginTime) / 1000000;

			if(success)
			{
				if(Emulator.COUTING)
				{
					if(txn.isReadOnly())
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

	public int getTotalOperations()
	{
		return this.successCounterRead + this.successCounterWrite;
	}

	public int getAbortCounter()
	{
		return this.abortCounter;
	}

}
