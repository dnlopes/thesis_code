package applications.tpcc;


import applications.BaseBenchmarkOptions;
import applications.BenchmarkOptions;
import applications.Transaction;
import applications.util.TransactionRecord;
import client.jdbc.CRDTConnectionFactory;
import common.util.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 05/06/15.
 */
public class TPCCClientEmulator implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(TPCCClientEmulator.class);

	private Connection connection;
	private int id;
	private final BaseBenchmarkOptions options;

	private TPCCStatistics stats;
	private long execLatency, commitLatency;
	private int benchmarkDuration;

	public TPCCClientEmulator(int id, BaseBenchmarkOptions options)
	{
		this.id = id;
		this.options = options;
		this.stats = new TPCCStatistics(this.id);

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

		this.benchmarkDuration = this.options.getDuration();
	}

	@Override
	public void run()
	{
		long startTime = System.currentTimeMillis();
		long rampUpTime;

		LOG.info("client {} started ramping up", id);

		while((rampUpTime = System.currentTimeMillis() - startTime) < BenchmarkOptions.Defaults.RAMPUP_TIME * 1000)
		{
			// RAMP UP TIME
			Transaction txn = this.options.getWorkload().getNextTransaction(options);
			boolean success = false;

			while(!success)
			{
				success = tryTransaction(txn);

				if(!success)
				{
					String error = txn.getLastError();
					if((!error.contains("try restarting transaction")) && (!error.contains("Duplicate entry")))
						LOG.error(error);
				}
			}
		}

		LOG.info("client {} ended ramp up time and will start the actual experiment", id);

		startTime = System.currentTimeMillis();
		long benchmarkTime;


		System.out.println("starting actual experiment");

		while((benchmarkTime = System.currentTimeMillis() - startTime) < benchmarkDuration * 1000)
		{
			// benchmark time here
			Transaction txn = this.options.getWorkload().getNextTransaction(options);
			execLatency = 0;
			commitLatency = 0;
			boolean success = false;

			while(!success)
			{
				execLatency = 0;
				commitLatency = 0;
				success = tryTransaction(txn);

				if(success)
				{
					this.stats.addTxnRecord(txn.getName(),
							new TransactionRecord(txn.getName(), execLatency, commitLatency, true,
									TPCCEmulator.ITERATION));
				} else
				{
					String error = txn.getLastError();

					if(!error.contains("try restarting transaction"))
						this.stats.addTxnRecord(txn.getName(),
								new TransactionRecord(txn.getName(), false, TPCCEmulator.ITERATION));

					if((!error.contains("try restarting transaction")) && (!error.contains("Duplicate entry")))
						LOG.error(error);
				}
			}
		}

		LOG.info("client {} finished the actual experiment", id);
	}

	private boolean tryTransaction(Transaction trx)
	{
		long beginExec = System.nanoTime();
		boolean execSuccess = trx.executeTransaction(this.connection);
		long execTime = System.nanoTime() - beginExec;
		execLatency += execTime;

		if(!execSuccess)
			return false;

		long beginCommit = System.nanoTime();
		boolean commitSuccess = trx.commitTransaction(this.connection);
		long commitTime = System.nanoTime() - beginCommit;
		commitLatency += commitTime;

		return commitSuccess;
	}

	public TPCCStatistics getStats()
	{
		return this.stats;
	}

}
