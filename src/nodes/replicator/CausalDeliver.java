package nodes.replicator;


import nodes.Deliver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.operation.ShadowTransaction;
import util.defaults.Configuration;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 23/05/15.
 */
public class CausalDeliver implements Deliver
{

	private static final Logger LOG = LoggerFactory.getLogger(CausalDeliver.class);
	private static final int THREAD_WAKEUP_INTERVAL = 500;

	private final Map<Integer, Queue<ShadowTransaction>> queues;
	private final Replicator replicator;

	public CausalDeliver(Replicator replicator)
	{
		this.replicator = replicator;
		this.queues = new HashMap<>();

		this.setup();

		DeliveryThread deliveryThread = new DeliveryThread();

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(deliveryThread, 0, THREAD_WAKEUP_INTERVAL, TimeUnit.MILLISECONDS);
	}

	private void setup()
	{
		int replicatorsNumber = Configuration.getInstance().getAllReplicatorsConfig().size();

		for(int i = 0; i < replicatorsNumber; i++)
		{
			Queue q = new PriorityBlockingQueue(10, new LogicalClockComparator(i));
			this.queues.put(i + 1, q); // i+1 because replicator id starts at 1 but clock index starts at 0
		}
	}

	@Override
	public void dispatchOperation(ShadowTransaction op)
	{
		if(this.canDeliver(op))
			replicator.deliverShadowTransaction(op);
		else
			this.addToQueue(op);
	}

	private void addToQueue(ShadowTransaction op)
	{
		int replicatorId = op.getReplicatorId();
		if(Configuration.DEBUG_ENABLED)
			LOG.debug("adding op with clock {} to queue", op.getClock().getClockValue());
		this.queues.get(replicatorId).add(op);
	}

	private boolean canDeliver(ShadowTransaction op)
	{
		LogicalClock opClock = op.getClock();
		return replicator.getCurrentClock().lessThanByAtMostOne(opClock);
	}

	private class LogicalClockComparator implements Comparator<ShadowTransaction>
	{

		private final int index;

		public LogicalClockComparator(int index)
		{
			this.index = index;
		}

		@Override
		public int compare(ShadowTransaction shadowTransaction1, ShadowTransaction shadowTransaction2)
		{
			LogicalClock clock1 = shadowTransaction1.getClock();
			LogicalClock clock2 = shadowTransaction2.getClock();
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
				for(Queue<ShadowTransaction> txnQueue : queues.values())
				{
					ShadowTransaction txn = txnQueue.poll();

					if(txn == null)
						continue;

					replicator.deliverShadowTransaction(txn);
					hasDelivered = true;
				}
			} while(hasDelivered);
		}
	}
}
