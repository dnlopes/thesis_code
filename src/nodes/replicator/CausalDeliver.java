package nodes.replicator;


import nodes.Deliver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.operation.ShadowOperation;
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
	private static final int THREAD_WAKEUP_INTERVAL = 1;

	private final Map<Integer, Queue<ShadowOperation>> queues;
	private final Replicator replicator;

	public CausalDeliver(Replicator replicator)
	{
		this.replicator = replicator;
		this.queues = new HashMap<>();

		this.setup();

		DeliveryThread deliveryThread = new DeliveryThread();

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(deliveryThread, 0, THREAD_WAKEUP_INTERVAL, TimeUnit.SECONDS);
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
	public void dispatchOperation(ShadowOperation op)
	{
		if(this.canDeliver(op))
			replicator.deliverShadowOperation(op);
		else
			this.addToQueue(op);
		/*{
			synchronized(this.pendingOperations)
			{
				this.pendingOperations.add(op);
			}
		}*/
	}

	private void addToQueue(ShadowOperation op)
	{
		int replicatorId = op.getReplicatorId();
		LOG.debug("adding op with clock {} to queue", op.getClock().getClockValue());
		this.queues.get(replicatorId).add(op);
	}

	private boolean canDeliver(ShadowOperation op)
	{
		LogicalClock opClock = op.getClock();
		return replicator.getCurrentClock().lessThanByAtMostOne(opClock);
	}

	private class LogicalClockComparator implements Comparator<ShadowOperation>
	{

		private final int index;

		public LogicalClockComparator(int index)
		{
			this.index = index;
		}

		@Override
		public int compare(ShadowOperation shadowOp1, ShadowOperation shadowOp2)
		{
			LogicalClock clock1 = shadowOp1.getClock();
			LogicalClock clock2 = shadowOp2.getClock();
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

			while(hasDelivered)
			{
				for(Queue<ShadowOperation> opQueue : queues.values())
				{
					ShadowOperation op = opQueue.peek();

					if(op == null)
						continue;

					if(canDeliver(op))
					{
						opQueue.poll();
						replicator.deliverShadowOperation(op);
						hasDelivered = true;
					}
				}
			}
		}
	}
}
