package server.agents.deliver;


import common.util.Topology;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.LogicalClock;
import common.thrift.CRDTCompiledTransaction;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 23/05/15.
 */
public class CausalDeliverAgent implements DeliverAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(CausalDeliverAgent.class);
	private static final int THREAD_WAKEUP_INTERVAL = 500;

	private final Map<Integer, Queue<CRDTCompiledTransaction>> queues;
	private final Replicator replicator;
	private final ScheduledExecutorService scheduleService;

	public CausalDeliverAgent(Replicator replicator)
	{
		this.replicator = replicator;
		this.queues = new HashMap<>();

		this.setup();

		DeliveryThread deliveryThread = new DeliveryThread();

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.scheduleService.scheduleAtFixedRate(deliveryThread, 0, THREAD_WAKEUP_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void setup()
	{
		int replicatorsNumber = Topology.getInstance().getReplicatorsCount();

		for(int i = 0; i < replicatorsNumber; i++)
		{

			Queue q = new PriorityBlockingQueue(100, new LogicalClockComparator(i));
			this.queues.put(i + 1, q); // i+1 because replicator id starts at 1 but clock index starts at 0
		}
	}

	@Override
	public void deliverTransaction(CRDTCompiledTransaction op)
	{
		if(canDeliver(op))
			this.replicator.deliverTransaction(op);
		else
			addToQueue(op);
	}

	private void addToQueue(CRDTCompiledTransaction op)
	{
		int replicatorId = op.getReplicatorId();

		if(LOG.isTraceEnabled())
			LOG.trace("adding op with clock {} to queue", op.getTxnClock());

		this.queues.get(replicatorId).add(op);
	}

	private boolean canDeliver(CRDTCompiledTransaction op)
	{
		LogicalClock opClock = new LogicalClock(op.getTxnClock());
		return replicator.getCurrentClock().lessThanByAtMostOne(opClock);
	}

	private class LogicalClockComparator implements Comparator<CRDTCompiledTransaction>
	{

		private final int index;

		public LogicalClockComparator(int index)
		{
			this.index = index;
		}

		@Override
		public int compare(CRDTCompiledTransaction shadowTransaction1, CRDTCompiledTransaction shadowTransaction2)
		{
			LogicalClock clock1 = new LogicalClock(shadowTransaction1.getTxnClock());
			LogicalClock clock2 = new LogicalClock(shadowTransaction2.getTxnClock());
			long entry1 = clock1.getEntry(this.index);
			long entry2 = clock2.getEntry(this.index);

			if(entry1 == entry2)
				return 0;
			if(entry1 > entry2)
				return 1;
			else
				return -1;
		}
	}


	private class DeliveryThread implements Runnable
	{
		@Override
		public void run()
		{
			boolean hasDelivered = false;

			do
			{
				for(Queue<CRDTCompiledTransaction> txnQueue : queues.values())
				{
					CRDTCompiledTransaction txn = txnQueue.poll();

					if(txn == null)
						continue;

					replicator.deliverTransaction(txn);
					hasDelivered = true;
				}
			} while(hasDelivered);
		}
	}
}
