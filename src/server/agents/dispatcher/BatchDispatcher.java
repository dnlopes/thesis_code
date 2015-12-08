package server.agents.dispatcher;


import common.thrift.CRDTPreCompiledTransaction;
import server.replicator.IReplicatorNetwork;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;


/**
 * Created by dnlopes on 21/10/15.
 * A SimpleBatchDisptacher simply groups transactions in a batch and, periodically, sends the batch to remote
 * replicators
 */
public class BatchDispatcher implements DispatcherAgent
{
	
	private static final Logger LOG = LoggerFactory.getLogger(BatchDispatcher.class);
	private static final int THREAD_WAKEUP_INTERVAL = 2000;

	private final IReplicatorNetwork networkInterface;
	private final ScheduledExecutorService scheduleService;
	private Queue<CRDTPreCompiledTransaction> pendingTransactions;

	public BatchDispatcher(Replicator replicator)
	{
		this.networkInterface = replicator.getNetworkInterface();
		this.pendingTransactions = new ConcurrentLinkedQueue<>();

		DispatcherThread deliveryThread = new DispatcherThread();

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.scheduleService.scheduleAtFixedRate(deliveryThread, 0, THREAD_WAKEUP_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void dispatchTransaction(CRDTPreCompiledTransaction op)
	{
		//TODO fix bug: check concurrency with dispatcher thread
		this.pendingTransactions.add(op);
	}

	private class DispatcherThread implements Runnable
	{

		@Override
		public void run()
		{
			Queue<CRDTPreCompiledTransaction> snapshot = pendingTransactions;
			pendingTransactions = new ConcurrentLinkedQueue<>();

			List<CRDTPreCompiledTransaction> batch = prepareBatch(snapshot);

			LOG.info("sending txn batch (size {}) to remote nodes", batch.size());
			networkInterface.sendBatchToRemote(batch);
		}

		private List<CRDTPreCompiledTransaction> prepareBatch(Queue<CRDTPreCompiledTransaction> transactions)
		{
			List<CRDTPreCompiledTransaction> batchList = new ArrayList<>();

			for(CRDTPreCompiledTransaction txn : transactions)
				batchList.add(txn);

			return batchList;
		}
	}
}