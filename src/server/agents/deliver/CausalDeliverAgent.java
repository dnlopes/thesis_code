package server.agents.deliver;


import common.thrift.CRDTPreCompiledTransaction;
import common.util.Topology;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.LogicalClock;
import server.util.TransactionCommitFailureException;

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

	private final Map<Integer, Queue<CRDTPreCompiledTransaction>> queues;
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
	public void deliverTransaction(CRDTPreCompiledTransaction op) throws TransactionCommitFailureException
	{
		if(canDeliver(op))
			this.replicator.deliverTransaction(op);
		else
			addToQueue(op);
	}

	private void addToQueue(CRDTPreCompiledTransaction op)
	{
		int replicatorId = op.getReplicatorId();

		LOG.trace("adding op with clock {} to queue", op.getTxnClock());

		this.queues.get(replicatorId).add(op);
	}

	private boolean canDeliver(CRDTPreCompiledTransaction op)
	{
		LogicalClock opClock = new LogicalClock(op.getTxnClock());
		return replicator.getCurrentClock().lessThanByAtMostOne(opClock);
	}

	private class LogicalClockComparator implements Comparator<CRDTPreCompiledTransaction>
	{

		private final int index;

		public LogicalClockComparator(int index)
		{
			this.index = index;
		}

		@Override
		public int compare(CRDTPreCompiledTransaction transaction1, CRDTPreCompiledTransaction transaction2)
		{
			LogicalClock clock1 = new LogicalClock(transaction1.getTxnClock());
			LogicalClock clock2 = new LogicalClock(transaction2.getTxnClock());
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

		private int id = replicator.getConfig().getId();
		private int counter = 0;

		@Override
		public void run()
		{
			counter++;

			if(counter % 15 == 0)
			{
				counter = 0;
				StringBuffer buffer = new StringBuffer("(");

				for(Queue<CRDTPreCompiledTransaction> txnQueue : queues.values())
				{
					buffer.append(txnQueue.size());
					buffer.append(",");
				}

				if(buffer.charAt(buffer.length() - 1) == ',')
					buffer.setLength(buffer.length() - 1);

				buffer.append(")");

				LOG.info("<r{}> pending queue size: {}", id, buffer.toString());
			}

			boolean hasDelivered = false;
			int cycles = 0;

			do
			{
				for(Queue<CRDTPreCompiledTransaction> txnQueue : queues.values())
				{
					CRDTPreCompiledTransaction txn = txnQueue.poll();

					if(txn == null)
						continue;

					try
					{
						replicator.deliverTransaction(txn);
					} catch(TransactionCommitFailureException e)
					{
						e.printStackTrace();
					}
					hasDelivered = true;
				}

				cycles++;
				if(cycles > BACKGROUND_MAX_IN_A_ROW_CYCLES)
					break;

			} while(hasDelivered);
		}
	}
}
