package nodes.proxy;


import database.scratchpad.DBScratchPad;
import database.scratchpad.IDBScratchPad;
import database.scratchpad.ScratchpadException;
import nodes.AbstractNode;
import nodes.NodeConfig;
import runtime.RuntimeUtils;
import runtime.IdentifierFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.ObjectPool;
import util.defaults.Configuration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Proxy extends AbstractNode
{

	private static final Logger LOG = LoggerFactory.getLogger(Proxy.class);
	private static int TXN_COUNT = 0;
	private static final int FREQUENCY = 150;
	private static final int TEMPORARY_SCRATCHPAD_POOL_SIZE = Integer.parseInt(System.getProperty("usersNum")) + 5;

	private final ObjectPool<IDBScratchPad> scratchpadsPool;
	// associates a connection id with the corresponding scratchpad
	private final ConcurrentHashMap<Integer, IDBScratchPad> activeScratchpads;
	private final IProxyNetwork networkInterface;
	private final AtomicInteger transactionsCounter;
	private final AtomicInteger scratchpadsCount;
	private final AtomicInteger connectionsCounter;
	// a small hack to avoid casting the same object over and over
	private ProxyConfig privateConfig;

	public Proxy(NodeConfig config)
	{
		super(config);

		this.privateConfig = (ProxyConfig) config;

		this.scratchpadsPool = new ObjectPool<>();
		this.activeScratchpads = new ConcurrentHashMap<>();
		this.networkInterface = new ProxyNetwork(this.privateConfig);
		this.transactionsCounter = new AtomicInteger();
		this.scratchpadsCount = new AtomicInteger();
		this.connectionsCounter = new AtomicInteger();

		IdentifierFactory.createGenerators(this.config);

		this.setup();
		System.out.println("proxy " + this.config.getId() + " online");
	}

	public int assignConnectionId()
	{
		return this.connectionsCounter.incrementAndGet();
	}

	public ResultSet executeQuery(String op, int connectionId) throws SQLException
	{
		IDBScratchPad pad;

		if(!this.connectionIsActive(connectionId))
			pad = this.beginTransaction(connectionId);
		else
			pad = this.activeScratchpads.get(connectionId);

		return pad.executeQuery(op);
	}

	public int executeUpdate(String op, int connectionId) throws SQLException
	{
		IDBScratchPad pad;

		if(!this.connectionIsActive(connectionId))
			pad = this.beginTransaction(connectionId);
		else
			pad = this.activeScratchpads.get(connectionId);

		return pad.executeUpdate(op);
	}

	public void commit(int connectionId) throws SQLException
	{
		IDBScratchPad pad = this.activeScratchpads.get(connectionId);

		/* if does not contain the txn, it means the transaction was not yet created
		 i.e no statements were executed. Thus, it should commit in every case */
		if(pad == null)
			return;

		TXN_COUNT++;

		if(TXN_COUNT % FREQUENCY == 0)
			if(LOG.isInfoEnabled())
				LOG.info("committing txn {}", pad.getActiveTransaction().getTxnId());

		pad.commitTransaction(this.networkInterface);

		this.activeScratchpads.remove(connectionId);
		this.scratchpadsPool.returnObject(pad);
	}

	public void abort(int connectionId)
	{
		IDBScratchPad pad = this.activeScratchpads.get(connectionId);

		if(pad == null)
			return;

		this.activeScratchpads.remove(connectionId);
		this.scratchpadsPool.returnObject(pad);
	}

	public void closeTransaction(int connectionId) throws SQLException
	{
		IDBScratchPad pad = this.activeScratchpads.get(connectionId);

		if(pad == null)
			return;

		if(!pad.getActiveTransaction().isReadOnly())
			this.commit(connectionId);
		else
		{
			this.activeScratchpads.remove(connectionId);
			this.scratchpadsPool.returnObject(pad);
		}
	}

	public IDBScratchPad beginTransaction(int connectionId)
	{
		IDBScratchPad pad = null;

		if(!this.activeScratchpads.containsKey(connectionId)) // txn is about to begin
		{
			pad = this.scratchpadsPool.borrowObject();
			if(pad == null)
			{
				LOG.warn("scratchpad pool was empty");
				try
				{
					pad = new DBScratchPad(this.scratchpadsCount.incrementAndGet(), this.privateConfig);
				} catch(SQLException | ScratchpadException e)
				{
					LOG.error("failed to initialize scratchpad: {}", e.getMessage());
					RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
				}
			}
		}

		int txnId = transactionsCounter.incrementAndGet();
		this.activeScratchpads.put(connectionId, pad);

		pad.startTransaction(txnId);
		return pad;
	}

	private void setup()
	{
		Configuration conf = Configuration.getInstance();

		for(int i = 0; i < TEMPORARY_SCRATCHPAD_POOL_SIZE; i++)
		{
			try
			{
				IDBScratchPad scratchpad = new DBScratchPad(i, (ProxyConfig) config);
				this.scratchpadsPool.addObject(scratchpad);
				this.scratchpadsCount.incrementAndGet();
			} catch(ScratchpadException | SQLException e)
			{
				LOG.error("failed to create scratchpad with id {}", i, e);
				RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
			}
		}

		if(LOG.isInfoEnabled())
			LOG.info("{} scratchpads available for temporary execution", this.scratchpadsPool.getPoolSize());
	}

	private boolean connectionIsActive(int connectionId)
	{
		return this.activeScratchpads.containsKey(connectionId);
	}

}
