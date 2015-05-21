package network.replicator;


import database.scratchpad.DBCommitPad;
import database.scratchpad.IDBCommitPad;
import network.AbstractNode;
import network.AbstractNodeConfig;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import util.ObjectPool;
import util.defaults.Configuration;
import runtime.operation.ShadowOperation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{
	
	private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);
	private LogicalClock clock;
	private static int REPLICATOR_ID;

	private IReplicatorNetwork networkInterface;
	private ReplicatorServerThread serverThread;
	private ObjectPool<IDBCommitPad> commitPadPool;

	private Map<String, ReplicatorConfig> otherReplicators;
	//saves all txn already committed
	private Set<Integer> committedTxns;
	
	public Replicator(AbstractNodeConfig config)
	{
		super(config);
		this.clock = new LogicalClock(Configuration.getInstance().getAllReplicatorsConfig().size());
		REPLICATOR_ID = this.config.getId();

		this.otherReplicators = new HashMap<>();
		this.networkInterface = new ReplicatorNetwork(this.getConfig());

		for(ReplicatorConfig allReplicators : Configuration.getInstance().getAllReplicatorsConfig().values())
			this.otherReplicators.put(allReplicators.getName(), allReplicators);

		this.committedTxns = new HashSet<>();
		this.commitPadPool = new ObjectPool<>();

		this.setup();

		try
		{
			this.serverThread = new ReplicatorServerThread(this);
			new Thread(this.serverThread).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}", this.getConfig().getName());
			e.printStackTrace();
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
		if(this.alreadyCommitted(shadowOperation.getTxnId()))
		{
			LOG.warn("duplicated transaction {}. Silently ignored.", shadowOperation.getTxnId());
			return true;
		}

		IDBCommitPad pad = this.commitPadPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("commitpad pool was empty");
			pad = new DBCommitPad(this);
		}

		boolean commitDecision = pad.commitShadowOperation(shadowOperation);

		if(commitDecision)
			this.committedTxns.add(shadowOperation.getTxnId());
		 else
			LOG.error("something went very wrong. State will not converge because op didnt commit");

		this.commitPadPool.returnObject(pad);

		return commitDecision;
	}

	private boolean alreadyCommitted(int txnId)
	{
		return this.committedTxns.contains(txnId);
	}

	public IReplicatorNetwork getNetworkInterface()
	{
		return this.networkInterface;
	}

	private void setup()
	{
		Configuration conf = Configuration.getInstance();
		int count = conf.getProxies().size() * 2;

		for(int i = 0; i < count; i++)
		{
			IDBCommitPad commitPad = new DBCommitPad(this);
			this.commitPadPool.addObject(commitPad);
		}
	}

	public LogicalClock getNextClock()
	{
		synchronized(this)
		{
			LogicalClock newClock = new LogicalClock(this.clock.getDcEntries());
			newClock.increment(REPLICATOR_ID);
			return newClock;
		}
	}

	public void mergeWithRemoteClock(LogicalClock clock)
	{
		synchronized(this)
		{
			LOG.info("merging clocks {} with {}", this.clock.toString(), clock.toString());
			this.clock = this.clock.maxClock(clock);
			LOG.info("merged clock is {}", this.clock.toString());
		}
	}
}
