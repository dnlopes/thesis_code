package server.replicator;


import common.util.*;
import common.util.defaults.ScratchpadDefaults;
import common.util.exception.InitComponentFailureException;
import common.util.exception.InvalidConfigurationException;
import org.apache.commons.lang3.StringUtils;
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
import server.hook.PrinterHook;
import server.util.CompilePreparationException;
import server.util.LogicalClock;
import common.util.defaults.ReplicatorDefaults;
import common.thrift.*;
import server.util.TransactionCommitFailureException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{
	
	private static final Logger LOG = LoggerFactory.getLogger(Replicator.class);
	private static final String SUBPREFIX = "r";
	private static final DatabaseMetadata metadata = Environment.DB_METADATA;

	private LogicalClock clock;
	private final IReplicatorNetwork networkInterface;
	private final ObjectPool<DBCommitter> agentsPool;
	private final Lock clockLock;
	private final String prefix;
	private final AtomicInteger txnCounter;
	protected final StatsCollector statsCollector;

	private final IDsManager idsManager;

	private final GarbageCollector garbageCollector;
	private final ScheduledExecutorService scheduleService;
	private final DeliverAgent deliver;
	private final DispatcherAgent dispatcher;
	private final CoordinationAgent coordAgent;

	public Replicator(NodeConfig config) throws InitComponentFailureException, InvalidConfigurationException
	{
		super(config);
		this.prefix = SUBPREFIX + this.config.getId() + "_";
		this.txnCounter = new AtomicInteger();
		this.networkInterface = new ReplicatorNetwork(this.config);

		this.deliver = AgentsFactory.getDeliverAgent(this);
		this.dispatcher = AgentsFactory.getDispatcherAgent(this);
		this.coordAgent = new SimpleCoordinationAgent(this);

		this.clock = new LogicalClock(Topology.getInstance().getReplicatorsCount());
		this.agentsPool = new ObjectPool<>();
		this.clockLock = new ReentrantLock();
		this.statsCollector = new StatsCollector();

		this.idsManager = new IDsManager(getPrefix(), getConfig());

		this.scheduleService = Executors.newScheduledThreadPool(2);
		this.garbageCollector = new GarbageCollector(this);
		this.scheduleService.scheduleAtFixedRate(garbageCollector, 0,
				ReplicatorDefaults.GARBAGE_COLLECTOR_THREAD_INTERVAL, TimeUnit.MILLISECONDS);
		this.scheduleService.scheduleAtFixedRate(new StateChecker(),
				ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL * 4, ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL,
				TimeUnit.MILLISECONDS);

		//deleteScratchpads();
		createCommiterAgents();

		try
		{
			new Thread(new ReplicatorServerThread(this)).start();
		} catch(TTransportException e)
		{
			String error = "failed to create background thread for replicator: " + e.getMessage();
			throw new InitComponentFailureException(error);
		}

		Runtime.getRuntime().addShutdownHook(new PrinterHook(this.statsCollector));
		System.out.println("replicator " + this.config.getId() + " online");
	}

	/**
	 * Attempts to commit a transaction.
	 *
	 * @param shadowTransaction
	 *
	 * @return true if it was sucessfully committed locally, false otherwise
	 */
	public Status commitOperation(CRDTPreCompiledTransaction txn) throws TransactionCommitFailureException
	{
		DBCommitter pad = this.agentsPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("commitpad pool was empty. creating new one");
			try
			{
				pad = new DBCommitterAgent(this.config);
			} catch(SQLException e)
			{
				LOG.warn("failed to create new commitpad at runtime", e);
			}

			if(pad == null)
				pad = this.getCommitPadLoop();
		}

		LOG.trace("txn {} from replicator {} committing on main storage ", txn.getId(), txn.getReplicatorId());

		Status status = pad.commitCrdtOperation(txn);

		if(!status.isSuccess())
			LOG.error(status.getError());

		this.agentsPool.returnObject(pad);

		return status;
	}

	public IReplicatorNetwork getNetworkInterface()
	{
		return this.networkInterface;
	}

	public LogicalClock getNextClock()
	{
		this.clockLock.lock();

		this.clock.increment(this.config.getId() - 1);
		LogicalClock newClock = new LogicalClock(this.clock.getDcEntries());

		this.clockLock.unlock();

		return newClock;
	}

	public void deliverTransaction(CRDTPreCompiledTransaction txn) throws TransactionCommitFailureException
	{
		mergeWithRemoteClock(new LogicalClock(txn.getTxnClock()));
		commitOperation(txn);
	}

	public void prepareToCommit(CRDTPreCompiledTransaction transaction) throws CompilePreparationException
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
				throw new CompilePreparationException("unexpected datafield type");
		}

		Map<String, SymbolEntry> symbolsMap = transaction.getSymbolsMap();

		for(CRDTPreCompiledOperation op : transaction.getOpsList())
		{
			replacePlaceHolders(op, symbolsMap, transaction);
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

	private void appendPrefixs(CRDTPreCompiledOperation op)
	{
		/*
		TODO later: implement
		DatabaseTable dbTable = METADATA.getTable(op.getTableName());

		for(UniqueConstraint constraint : dbTable.getUniqueConstraints())
		{

		}
		*/
	}

	private void replacePlaceHolders(CRDTPreCompiledOperation op, Map<String, SymbolEntry> symbolsMap,
									 CRDTPreCompiledTransaction transaction) throws CompilePreparationException
	{
		String sqlOp = op.getSqlOp();
		//StringBuilder builder = new StringBuilder("'").append(transaction.getTxnClock()).append("'");
		String clockReplacer = transaction.getTxnClock();

		String tempOp = StringUtils.replace(sqlOp, LogicalClock.CLOCK_PLACEHOLDER, clockReplacer);
		op.setSqlOp(tempOp);

		if(op.isSetSymbols())
		{
			Set<String> symbolsSet = op.getSymbols();

			for(String symbol : symbolsSet)
			{
				String realValue = symbolsMap.get(symbol).getRealValue();

				if(realValue == null)
					throw new CompilePreparationException("failed to retrieve id from coordinator");

				tempOp = StringUtils.replace(tempOp, symbol, realValue);
				op.setSqlOp(tempOp);
			}
		}
	}

	private void mergeWithRemoteClock(LogicalClock clock)
	{
		LOG.trace("merging clocks {} with {}", this.clock.toString(), clock.toString());

		this.clockLock.lock();
		this.clock = this.clock.maxClock(clock);
		this.clockLock.unlock();
	}

	private DBCommitter getCommitPadLoop()
	{
		int counter = 0;
		DBCommitter pad;

		do
		{
			pad = this.agentsPool.borrowObject();
			counter++;

			if(counter % 150 == 0)
			{
				LOG.warn("already tried {} to get commitpad from pool", counter);
				counter = 0;
			}

		} while(pad == null);

		return pad;
	}

	private void createCommiterAgents()
	{
		int agentsNumber = Environment.COMMIT_PAD_POOL_SIZE;

		for(int i = 0; i < agentsNumber; i++)
		{
			try
			{
				DBCommitter agent = new DBCommitterAgent(getConfig());

				if(agent != null)
					this.agentsPool.addObject(agent);

			} catch(SQLException e)
			{
				LOG.warn(e.getMessage(), e);
			}
		}

		LOG.info("{} commit agents available for main storage execution", this.agentsPool.getPoolSize());
	}

	private void deleteScratchpads()
	{
		int id = 1;
		try
		{
			boolean keepGoing = true;
			Connection con = ConnectionFactory.getDefaultConnection(config);
			do
			{
				keepGoing = deleteSingleScratchpads(con, id++);
			} while(keepGoing);

		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private boolean deleteSingleScratchpads(Connection con, int id) throws SQLException
	{
		Statement stat = con.createStatement();
		Collection<DatabaseTable> allTables = Environment.DB_METADATA.getAllTables();

		for(DatabaseTable table : allTables)
		{
			try
			{
				StringBuilder buffer = new StringBuilder("DROP TABLE ");
				buffer.append(ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX).append(table.getName());
				buffer.append("_").append(id);
				String sql = buffer.toString();
				stat.execute(sql);
				con.commit();
			} catch(SQLException e)
			{
				LOG.debug("no more scratchpads to delete");
				return false;
			}
		}

		return true;
	}


	public int assignNewTransactionId()
	{
		return this.txnCounter.incrementAndGet();
	}

	private class StateChecker implements Runnable
	{

		private int id = config.getId();

		@Override
		public void run()
		{
			LOG.info("<r{}> vector clock: {}", id, clock.toString());
		}
	}
}
