package nodes.replicator.dispatcher;


import nodes.replicator.IReplicatorNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CRDTTransaction;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 08/10/15.
 */
public class AggregatorDispatcher implements DispatcherAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(AggregatorDispatcher.class);
	private static final int THREAD_WAKEUP_INTERVAL = 500;

	private final IReplicatorNetwork networkInterface;
	private final ScheduledExecutorService scheduleService;
	private Queue<CRDTTransaction> pendingTransactions;

	public AggregatorDispatcher(IReplicatorNetwork networkInterface)
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
			//TODO implement merge
			// 1) iterate over pending
			// 2) merge
			// 3) compile
			// 4) send
			networkInterface.sendOperationToRemote(null);
		}
	}

}
