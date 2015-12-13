package applications.tpcc;


import applications.BaseBenchmarkOptions;
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
	}

	@Override
	public void run()
	{
		while(TPCCEmulator.RUNNING)
		{
			Transaction txn = this.options.getWorkload().getNextTransaction(options);
			execLatency = 0;
			commitLatency = 0;
			boolean success = false;

			while(!success)
			{
				//execLatency = 0;
				//commitLatency = 0;
				success = tryTransaction(txn);

				if(success)
				{
					if(TPCCEmulator.COUTING)
						this.stats.addTxnRecord(txn.getName(),
								new TransactionRecord(txn.getName(), execLatency, commitLatency, true));
				} else
				{
					String error = txn.getLastError();

					if(!error.contains("try restarting transaction") && TPCCEmulator.COUTING)
						this.stats.addTxnRecord(txn.getName(), new TransactionRecord(txn.getName(), false));

					if((!error.contains("try restarting transaction")) && (!error.contains("Duplicate entry")))
						LOG.error(error);
				}
			}
		}
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

	public void setStats(TPCCStatistics stats)
	{
		this.stats = stats;
	}

}
