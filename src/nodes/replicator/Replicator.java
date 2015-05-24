package nodes.replicator;


import database.scratchpad.DBCommitPad;
import database.scratchpad.IDBCommitPad;
import nodes.AbstractNode;
import nodes.AbstractNodeConfig;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import util.ObjectPool;
import util.defaults.Configuration;
import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{
	
	private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

	private LogicalClock clock;
	private IReplicatorNetwork networkInterface;
	private ObjectPool<IDBCommitPad> commitPadPool;

	public Replicator(AbstractNodeConfig config)
	{
		super(config);

		this.clock = new LogicalClock(Configuration.getInstance().getAllReplicatorsConfig().size());
		this.networkInterface = new ReplicatorNetwork(this.config);
		this.commitPadPool = new ObjectPool<>();

		this.setupPads();

		try
		{
			new Thread(new ReplicatorServerThread(this)).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}: ", this.getConfig().getName(), e);
		}

		LOG.info("replicator {} online", this.config.getId());
	}

	/**
	 * Attempts to commit a shadow operation.
	 *
	 * @param shadowOperation
	 *
	 * @return true if it was sucessfully committed locally, false otherwise
	 */
	public boolean commitOperation(ShadowOperation shadowOperation)
	{
		IDBCommitPad pad = this.commitPadPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("commitpad pool was empty");
			pad = new DBCommitPad(this.config);
		}

		boolean commitDecision = pad.commitShadowOperation(shadowOperation);

		if(!commitDecision)
			LOG.error("something went very wrong. State will not converge because operation failed to commit");

		this.commitPadPool.returnObject(pad);

		return commitDecision;
	}

	public IReplicatorNetwork getNetworkInterface()
	{
		return this.networkInterface;
	}

	private void setupPads()
	{
		Configuration conf = Configuration.getInstance();
		int count = conf.getProxies().size() * 2;

		for(int i = 0; i < count; i++)
		{
			IDBCommitPad commitPad = new DBCommitPad(this.getConfig());
			this.commitPadPool.addObject(commitPad);
		}
	}

	public LogicalClock getNextClock()
	{
		synchronized(this.clock)
		{
			LogicalClock newClock = new LogicalClock(this.clock.getDcEntries());
			this.clock = newClock;
			newClock.increment(this.config.getId() - 1);
			return newClock;
		}
	}

	public void mergeWithRemoteClock(LogicalClock clock)
	{
		synchronized(this.clock)
		{
			LOG.info("merging clocks {} with {}", this.clock.toString(), clock.toString());
			this.clock = this.clock.maxClock(clock);
			LOG.info("merged clock is {}", this.clock.toString());
		}
	}

	public void deliverShadowOperation(ShadowOperation shadowOp)
	{
		this.mergeWithRemoteClock(shadowOp.getClock());
		this.commitOperation(shadowOp);
	}

	public LogicalClock getCurrentClock()
	{
		return this.clock;
	}

}
