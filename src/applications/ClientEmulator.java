package applications;


import applications.util.ClientStatistics;
import client.jdbc.CRDTConnectionFactory;
import common.util.ConnectionFactory;
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
	private int id;
	private final BaseBenchmarkOptions options;
	private long sumReadLatency, sumWriteLatency;
	private int successCounterRead, successCounterWrite, abortCounter;
	private ClientStatistics stats;

	public ClientEmulator(int id, BaseBenchmarkOptions options)
	{
		this.id = id;
		this.options = options;
		this.sumReadLatency = 0;
		this.sumWriteLatency = 0;
		this.successCounterRead = 0;
		this.successCounterWrite = 0;
		this.abortCounter = 0;
		this.stats = new ClientStatistics(this.id);

		try
		{
			if(this.options.isCRDTDriver())
				this.connection = CRDTConnectionFactory.getCRDTConnection(this.options.getDbProps(),
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
			Transaction txn = this.options.getWorkload().getNextTransaction(options);

			long beginTime = System.nanoTime();
			boolean success = txn.executeTransaction(this.connection);
			long endTime = System.nanoTime();

			// in milliseconds
			long latency = (endTime - beginTime) / 1000000;

			if(success)
			{
				if(Emulator.COUTING)
				{
					this.stats.addLatency(latency);
					this.stats.incrementSuccess();

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
			{
				this.stats.incrementAborts();
				this.abortCounter++;
				LOG.error("txn {} failed: {}", txn.getName(), txn.getLastError());
			}
		}
	}

	public int getTotalOperations()
	{
		return this.successCounterRead + this.successCounterWrite;
	}

	public int getSuccessCounter()
	{
		return this.successCounterRead + this.successCounterWrite;
	}

	public float getAverageReadLatency()
	{
		if(this.successCounterRead == 0)
			return 0;

		return this.sumReadLatency / this.successCounterRead;
	}

	public float getAverageWriteLatency()
	{
		if(this.successCounterWrite == 0)
			return 0;

		return this.sumWriteLatency / this.successCounterWrite;
	}

	public float getAverageLatency()
	{
		return (this.getAverageReadLatency() * this.successCounterRead + this.getAverageWriteLatency() * this
				.successCounterWrite) / this.getSuccessCounter();
	}

	public int getAbortCounter()
	{
		return this.abortCounter;
	}

	public ClientStatistics getStats()
	{
		return this.stats;
	}
}
