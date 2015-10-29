package server.replicator;


import common.util.Environment;
import server.agents.AgentsFactory;
import server.execution.StatsCollector;
import server.execution.main.DBCommitterAgent;
import server.execution.main.DBCommitter;
import common.database.util.DatabaseMetadata;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.nodes.AbstractNode;
import common.nodes.NodeConfig;
import server.agents.coordination.SimpleCoordinationAgent;
import server.agents.coordination.CoordinationAgent;
import server.agents.deliver.DeliverAgent;
import server.agents.dispatcher.DispatcherAgent;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.agents.coordination.IDsManager;
import common.util.RuntimeUtils;
import server.util.LogicalClock;
import common.util.ExitCode;
import common.util.ObjectPool;
import common.Configuration;
import common.util.defaults.ReplicatorDefaults;
import common.thrift.*;

import java.util.Map;
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
	private static final String SUBPREFIX = "r";
	private static final DatabaseMetadata metadata = Configuration.getInstance().getDatabaseMetadata();

	private LogicalClock clock;
	private final IReplicatorNetwork networkInterface;
	private final ObjectPool<DBCommitter> agentsPool;
	private final Lock clockLock;
	private final String prefix;

	private final IDsManager idsManager;
	private final StatsCollector statsCollector;

	private final GarbageCollector garbageCollector;
	private final ScheduledExecutorService scheduleService;
	private final DeliverAgent deliver;
	private final DispatcherAgent dispatcher;
	private final CoordinationAgent coordAgent;

	public Replicator(NodeConfig config)
	{
		super(config);
		this.prefix = SUBPREFIX + this.config.getId() + "_";

		this.deliver = AgentsFactory.getDeliverAgent(this);
		this.dispatcher = AgentsFactory.getDispatcherAgent(this);
		this.coordAgent = new SimpleCoordinationAgent(this);

		this.clock = new LogicalClock(Configuration.getInstance().getReplicatorsCount());
		this.agentsPool = new ObjectPool<>();
		this.clockLock = new ReentrantLock();
		this.networkInterface = new ReplicatorNetwork(this.config);
		this.idsManager = new IDsManager(getPrefix(), getConfig());
		this.statsCollector = new StatsCollector();

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.garbageCollector = new GarbageCollector(this);
		this.scheduleService.scheduleAtFixedRate(garbageCollector, 0,
				ReplicatorDefaults.GARBAGE_COLLECTOR_THREAD_INTERVAL, TimeUnit.MILLISECONDS);

		createCommiterAgents();

		try
		{
			new Thread(new ReplicatorServerThread(this)).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}: ", getConfig().getName(), e);
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
			LOG.warn("commit pad pool was empty");
			pad = new DBCommitterAgent(this.config, this.statsCollector);
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

	public void deliverTransaction(CRDTCompiledTransaction txn)
	{
		mergeWithRemoteClock(new LogicalClock(txn.getTxnClock()));
		commitOperation(txn);
	}

	public void prepareToCommit(CRDTTransaction transaction)
	{
		if(!transaction.isSetSymbolsMap())
			return;

		Map<String, SymbolEntry> symbols = transaction.getSymbolsMap();

		//generate unique values locally
		for(SymbolEntry symbolEntry : symbols.values())
		{
			// already got value from coordinator
			if(symbolEntry.isSetRealValue())
				continue;

			// lets generate a unique value locally
			DatabaseTable dbTable = metadata.getTable(symbolEntry.getTableName());
			DataField dataField = dbTable.getField(symbolEntry.getFieldName());

			if(dataField.isNumberField() && dataField.isAutoIncrement())
				symbolEntry.setRealValue(String.valueOf(
						this.idsManager.getNextId(symbolEntry.getTableName(), symbolEntry.getFieldName())));
			else
				RuntimeUtils.throwRunTimeException("unexpected datafield type", ExitCode.INVALIDUSAGE);
		}

		Map<String, SymbolEntry> symbolsMap = transaction.getSymbolsMap();

		for(CRDTOperation op : transaction.getOpsList())
		{
			replaceSymbols(op, symbolsMap);
			appendPrefixs(op);
		}
	}

	public String getPrefix()
	{
		return this.prefix;
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

	private void appendPrefixs(CRDTOperation op)
	{
		/*
		TODO later: implement
		DatabaseTable dbTable = METADATA.getTable(op.getTableName());

		for(UniqueConstraint constraint : dbTable.getUniqueConstraints())
		{

		}
		*/
	}

	private void replaceSymbols(CRDTOperation op, Map<String, SymbolEntry> symbolsMap)
	{
		if(op.isSetSymbolFieldMap())
		{
			Map<String, String> opSymbolsMap = op.getSymbolFieldMap();

			for(Map.Entry<String, String> entry : opSymbolsMap.entrySet())
			{
				String realValue = symbolsMap.get(entry.getKey()).getRealValue();

				if(realValue == null)
					RuntimeUtils.throwRunTimeException("real value should not be null", ExitCode.NULLPOINTER);

				op.getNewFieldValues().put(entry.getValue(), realValue);
			}
		}
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
		int agentsNumber = Environment.COMMIT_PAD_POOL_SIZE;

		for(int i = 0; i < agentsNumber; i++)
		{
			DBCommitter agent = new DBCommitterAgent(getConfig(), this.statsCollector);

			if(agent != null)
				this.agentsPool.addObject(agent);
		}

		if(LOG.isInfoEnabled())
			LOG.info("{} commit agents available for main storage execution", this.agentsPool.getPoolSize());
	}
}
