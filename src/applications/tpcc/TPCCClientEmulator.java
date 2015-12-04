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
			long execLatency;
			long commitLatency = 0;

			Transaction txn = this.options.getWorkload().getNextTransaction(options);

			long beginTime = System.nanoTime();
			boolean success = txn.executeTransaction(this.connection);
			long endTime = System.nanoTime();
			//execLatency = (endTime - beginTime) / 1000000;
			execLatency = endTime - beginTime;

			if(success)
			{
				long beginTime2 = System.nanoTime();
				success = txn.commitTransaction(this.connection);
				long endTime2 = System.nanoTime();
				commitLatency = endTime2 - beginTime2;
				//commitLatency = (endTime2 - beginTime2) / 1000000;
			}

			if(success)
			{
				if(TPCCEmulator.COUTING)
				{
					this.stats.addTxnRecord(txn.getName(), new TransactionRecord(txn.getName(), execLatency,
							commitLatency,
							true));
					/*this.stats.addExecLatency(txn.getName(), execLatency);
					this.stats.addCommitLatency(txn.getName(), commitLatency);
					this.stats.addLatency(txn.getName(), execLatency + commitLatency);
					this.stats.incrementSuccess(txn.getName());*/
				}
			} else
			{
				//this.stats.incrementAborts(txn.getName());
				this.stats.addTxnRecord(txn.getName(), new TransactionRecord(txn.getName(), false));
				LOG.error("txn {} failed: {}", txn.getName(), txn.getLastError());
			}
		}
	}
	public TPCCStatistics getStats()
	{
		return this.stats;
	}
}
