package nodes.replicator;


import database.scratchpad.DBCommitPad;
import database.scratchpad.IDBCommitPad;
import nodes.AbstractNode;
import nodes.NodeConfig;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.ObjectPool;
import util.defaults.Configuration;
import util.thrift.ThriftShadowTransaction;

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
	private static final int GC_THREAD_WAKEUP_INTERVAL = 500;

	private LogicalClock clock;
	private IReplicatorNetwork networkInterface;
	private ObjectPool<IDBCommitPad> commitPadPool;
	private Lock clockLock;
	private GarbageCollector gc;

	public Replicator(NodeConfig config)
	{
		super(config);

		this.clock = new LogicalClock(Configuration.getInstance().getAllReplicatorsConfig().size());
		this.commitPadPool = new ObjectPool<>();
		this.clockLock = new ReentrantLock();
		this.networkInterface = new ReplicatorNetwork(this.config);

		try
		{
			new Thread(new ReplicatorServerThread(this)).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}: ", this.getConfig().getName(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.NOINITIALIZATION);
		}

		this.setupPads();
		this.gc = new GarbageCollector(this.networkInterface);

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(this.gc, 0, GC_THREAD_WAKEUP_INTERVAL , TimeUnit.MILLISECONDS);

		System.out.println("replicator " + this.config.getId() + " online");
	}

	/**
	 * Attempts to commit a shadow transaction.
	 *
	 * @param shadowTransaction
	 *
	 * @return true if it was sucessfully committed locally, false otherwise
	 */
	public boolean commitOperation(ThriftShadowTransaction shadowTransaction)
	{
		IDBCommitPad pad = this.commitPadPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("commitpad pool was empty");
			pad = new DBCommitPad(this.config);
		}

		boolean commitDecision = pad.commitShadowTransaction(shadowTransaction);

		if(!commitDecision)
			LOG.warn("something went very wrong. State will not converge because operation failed to commit");

		this.commitPadPool.returnObject(pad);

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

		if(Configuration.DEBUG_ENABLED)
			LOG.debug("clock incremented to {}", newClock.toString());

		return newClock;
	}

	public void deliverShadowTransaction(ThriftShadowTransaction shadowTransaction)
	{
		this.mergeWithRemoteClock(new LogicalClock(shadowTransaction.getClock()));
		this.commitOperation(shadowTransaction);
	}

	public LogicalClock getCurrentClock()
	{
		return this.clock;
	}

	private void mergeWithRemoteClock(LogicalClock clock)
	{
		if(Configuration.DEBUG_ENABLED)
			LOG.debug("merging clocks {} with {}", this.clock.toString(), clock.toString());

		this.clockLock.lock();
		this.clock = this.clock.maxClock(clock);
		this.clockLock.unlock();

		if(Configuration.DEBUG_ENABLED)
			LOG.debug("merged clock is {}", this.clock.toString());
	}

	private void setupPads()
	{
		Configuration conf = Configuration.getInstance();

		for(int i = 0; i < conf.getScratchpadPoolSize(); i++)
		{
			IDBCommitPad commitPad = new DBCommitPad(this.getConfig());
			this.commitPadPool.addObject(commitPad);
		}

		if(Configuration.INFO_ENABLED)
			LOG.info("{} commitpads available for main storage execution", this.commitPadPool.getPoolSize());
	}
}
