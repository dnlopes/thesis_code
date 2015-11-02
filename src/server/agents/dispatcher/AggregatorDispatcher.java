package server.agents.dispatcher;


import common.util.ExitCode;
import common.util.RuntimeUtils;
import server.replicator.IReplicatorNetwork;
import common.thrift.CRDTCompiledTransaction;
import common.thrift.CRDTTransaction;
import server.replicator.Replicator;

import java.util.*;
import java.util.concurrent.*;


/**
 * Created by dnlopes on 08/10/15.
 * AggregatorDispatcher caches incoming transactions and merges some of them when possible (for instance 2
 * consecutives updates to the same @LWW field). Periodically, transactions are sent as a batch to remote replicators
 */
public class AggregatorDispatcher implements DispatcherAgent
{

	private static final int THREAD_WAKEUP_INTERVAL = 500;

	private final IReplicatorNetwork networkInterface;
	private final ScheduledExecutorService scheduleService;
	private Queue<CRDTTransaction> pendingTransactions;

	public AggregatorDispatcher(Replicator replicator)
	{
		RuntimeUtils.throwRunTimeException("missing implementation", ExitCode.MISSING_IMPLEMENTATION);

		this.networkInterface = replicator.getNetworkInterface();
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
			//TODO implement merge
			// 1) iterate over pending
			// 2) merge
			// 3) compile
			// 4) send
			Queue<CRDTTransaction> snapshot = pendingTransactions;
			pendingTransactions = new PriorityBlockingQueue<>();

			List<CRDTCompiledTransaction> batch = this.prepareBatch(snapshot);

			networkInterface.sendBatchToRemote(batch);
		}

		private List<CRDTCompiledTransaction> prepareBatch(Queue<CRDTTransaction> transactions)
		{
			return null;
		}
	}

}