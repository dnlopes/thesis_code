package nodes.replicator.dispatcher;


import nodes.replicator.IReplicatorNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CRDTCompiledTransaction;
import util.thrift.CRDTTransaction;
import util.thrift.ThriftUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;


/**
 * Created by dnlopes on 21/10/15.
 * A BasicBatchDisptacher simply groups transactions in a batch and, periodically, sends the batch to remote replicators
 */
public class BasicBatchDispatcher implements DispatcherAgent
{
	
	private static final Logger LOG = LoggerFactory.getLogger(BasicBatchDispatcher.class);
	private static final int THREAD_WAKEUP_INTERVAL = 500;

	private final IReplicatorNetwork networkInterface;
	private final ScheduledExecutorService scheduleService;
	private Queue<CRDTTransaction> pendingTransactions;

	public BasicBatchDispatcher(IReplicatorNetwork networkInterface)
	{
		this.networkInterface = networkInterface;
		this.pendingTransactions = new ConcurrentLinkedQueue<>();

		DispatcherThread deliveryThread = new DispatcherThread();

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.scheduleService.scheduleAtFixedRate(deliveryThread, 0, THREAD_WAKEUP_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void dispatchTransaction(CRDTTransaction op)
	{
		this.pendingTransactions.add(op);
	}

	private class DispatcherThread implements Runnable
	{

		@Override
		public void run()
		{
			Queue<CRDTTransaction> snapshot = pendingTransactions;
			pendingTransactions = new PriorityBlockingQueue<>();

			List<CRDTCompiledTransaction> batch = this.prepareBatch(snapshot);

			networkInterface.sendBatchToRemote(batch);
		}

		private List<CRDTCompiledTransaction> prepareBatch(Queue<CRDTTransaction> transactions)
		{
			List<CRDTCompiledTransaction> batchList = new ArrayList<>();

			for(CRDTTransaction txn : transactions)
				batchList.add(ThriftUtils.compileCRDTTransaction(txn));

			return batchList;
		}
	}
}