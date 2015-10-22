package nodes.replicator;


import database.execution.main.DBCommitterAgent;
import database.execution.main.DBCommitter;
import nodes.AbstractNode;
import nodes.NodeConfig;
import nodes.replicator.coordination.BasicCoordinationAgent;
import nodes.replicator.coordination.CoordinationAgent;
import nodes.replicator.deliver.CausalDeliverAgent;
import nodes.replicator.deliver.DeliverAgent;
import nodes.replicator.dispatcher.BasicBatchDispatcher;
import nodes.replicator.dispatcher.DispatcherAgent;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.ObjectPool;
import util.Configuration;
import util.defaults.ReplicatorDefaults;
import util.thrift.CRDTCompiledTransaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{
	
	private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

	private LogicalClock clock;
	private final IReplicatorNetwork networkInterface;
	private final ObjectPool<DBCommitter> agentsPool;
	private final Lock clockLock;

	private final GarbageCollector garbageCollector;
	private final ScheduledExecutorService scheduleService;
	private final DeliverAgent deliver;
	private final DispatcherAgent dispatcher;
	private final CoordinationAgent coordAgent;

	public Replicator(NodeConfig config)
	{
		super(config);

		this.clock = new LogicalClock(Configuration.getInstance().getReplicatorsCount());
		this.agentsPool = new ObjectPool<>();
		this.clockLock = new ReentrantLock();
		this.networkInterface = new ReplicatorNetwork(this.config);

		this.deliver = new CausalDeliverAgent(this);
		this.dispatcher = new BasicBatchDispatcher(this);
		this.coordAgent = new BasicCoordinationAgent(this);

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.garbageCollector = new GarbageCollector(this);
		this.scheduleService.scheduleAtFixedRate(garbageCollector, 0,
				ReplicatorDefaults.GARBAGE_COLLECTOR_THREAD_INTERVAL, TimeUnit.MILLISECONDS);

		this.createCommiterAgents();

		try
		{
			new Thread(new ReplicatorServerThread(this)).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}: ", this.getConfig().getName(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.NOINITIALIZATION);
		}

		System.out.println("replicator " + this.config.getId() + " online");
	}

	/**
	 * Attempts to commit a shadow transaction.
	 *
	 * @param shadowTransaction
	 *
	 * @return true if it was sucessfully committed locally, false otherwise
	 */
	public boolean commitOperation(CRDTCompiledTransaction txn)
	{
		DBCommitter pad = this.agentsPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("commitpad pool was empty");
			pad = new DBCommitterAgent(this.config);
		}

		boolean commitDecision = pad.commitShadowTransaction(txn);

		if(!commitDecision)
			LOG.warn("something went very wrong. State will not converge because operation failed to commit");

		this.agentsPool.returnObject(pad);

		return commitDecision;
	}

	public IReplicatorNetwork getNetworkInterface()
	{
		return this.networkInterface;
	}

	public LogicalClock getNextClock()
	{
		this.clockLock.lock();

		LogicalClock newClock = new LogicalClock(this.clock.getDcEntries());
		this.clock = newClock;
		newClock.increment(this.config.getId() - 1);

		this.clockLock.unlock();

		if(LOG.isDebugEnabled())
			LOG.debug("clock incremented to {}", newClock.toString());

		return newClock;
	}

	public void deliverShadowTransaction(CRDTCompiledTransaction txn)
	{
		this.mergeWithRemoteClock(new LogicalClock(txn.getTxnClock()));
		this.commitOperation(txn);
	}

	public LogicalClock getCurrentClock()
	{
		return this.clock;
	}

	public DispatcherAgent getDispatcher()
	{
		return this.dispatcher;
	}

	public DeliverAgent getDeliver()
	{
		return this.deliver;
	}

	public CoordinationAgent getCoordAgent()
	{
		return this.coordAgent;
	}

	private void mergeWithRemoteClock(LogicalClock clock)
	{
		if(LOG.isDebugEnabled())
			LOG.debug("merging clocks {} with {}", this.clock.toString(), clock.toString());

		this.clockLock.lock();
		this.clock = this.clock.maxClock(clock);
		this.clockLock.unlock();

		if(LOG.isDebugEnabled())
			LOG.debug("merged clock is {}", this.clock.toString());
	}

	private void createCommiterAgents()
	{
		int agentsNumber = Configuration.getInstance().getCommitPadPoolSize();

		for(int i = 0; i < agentsNumber; i++)
		{
			DBCommitter agent = new DBCommitterAgent(this.getConfig());

			if(agent != null)
				this.agentsPool.addObject(agent);
		}

		if(LOG.isInfoEnabled())
			LOG.info("{} commit agents available for main storage execution", this.agentsPool.getPoolSize());
	}
}
