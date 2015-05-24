package nodes.proxy;


import database.scratchpad.DBScratchPad;
import database.scratchpad.IDBScratchPad;
import database.scratchpad.ScratchpadException;
import nodes.AbstractNode;
import nodes.AbstractNodeConfig;
import runtime.RuntimeUtils;
import runtime.IdentifierFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import runtime.txn.TransactionIdentifier;
import runtime.operation.DBSingleOperation;
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

	private ObjectPool<IDBScratchPad> scratchpadsPool;
	private ConcurrentHashMap<TransactionIdentifier, IDBScratchPad> activeScratchpads;
	private IProxyNetwork networkInterface;
	private AtomicInteger transactionsCounter;
	private AtomicInteger scratchpadsCount;
	// a small hack to avoid casting the same object over and over
	private ProxyConfig privateConfig;

	public Proxy(AbstractNodeConfig config)
	{
		super(config);

		this.privateConfig = (ProxyConfig) config;

		this.scratchpadsPool = new ObjectPool<>();
		this.activeScratchpads = new ConcurrentHashMap<>();
		this.networkInterface = new ProxyNetwork(this.config);
		this.transactionsCounter = new AtomicInteger();
		this.scratchpadsCount = new AtomicInteger();

		IdentifierFactory.createGenerators(this.config);

		this.setup();

		LOG.info("proxy {} online.", this.config.getId());
	}

	public ResultSet executeQuery(DBSingleOperation op, TransactionIdentifier txnId) throws SQLException
	{
		IDBScratchPad pad = this.activeScratchpads.get(txnId);
		return pad.executeQuery(op);
	}

	public int executeUpdate(DBSingleOperation op, TransactionIdentifier txnId) throws SQLException
	{
		IDBScratchPad pad = this.activeScratchpads.get(txnId);
		return pad.executeUpdate(op);
	}

	public boolean commit(TransactionIdentifier txnId)
	{
		IDBScratchPad pad = this.activeScratchpads.get(txnId);
		TXN_COUNT++;

		if(TXN_COUNT % FREQUENCY == 0)
			LOG.info("committing txn {}", pad.getActiveTransaction().getTxnId());

		/* if does not contain the txn, it means the transaction was not yet created
		 i.e no statements were executed. Thus, it should commit in every case */
		if(pad == null)
			return true;

		boolean commitResult = pad.commitTransaction(this.networkInterface);

		this.activeScratchpads.remove(txnId);
		this.scratchpadsPool.returnObject(pad);

		txnId.resetValue();

		return commitResult;
	}

	public void abort(TransactionIdentifier txnId)
	{
		IDBScratchPad pad = this.activeScratchpads.get(txnId);

		if(pad == null)
			return;

		this.activeScratchpads.remove(txnId);
		this.scratchpadsPool.returnObject(pad);
		txnId.resetValue();
	}

	public void closeTransaction(TransactionIdentifier txnId)
	{
		IDBScratchPad pad = this.activeScratchpads.get(txnId);

		if(pad == null)
			return;

		if(!pad.getActiveTransaction().isReadOnly())
			this.commit(txnId);
		else
		{
			this.activeScratchpads.remove(txnId);
			this.scratchpadsPool.returnObject(pad);

			LOG.trace("closing txn {}", txnId.getValue());
			txnId.resetValue();
		}
	}

	public void beginTransaction(TransactionIdentifier txnId)
	{
		txnId.setValue(this.transactionsCounter.incrementAndGet());

		IDBScratchPad pad;
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

		this.activeScratchpads.put(txnId, pad);
		pad.startTransaction(txnId);
	}

	private void setup()
	{
		for(int i = 1; i <= Configuration.getInstance().getScratchpadPoolSize(); i++)
		{
			try
			{
				IDBScratchPad scratchpad = new DBScratchPad(i, (ProxyConfig) config);
				this.scratchpadsPool.addObject(scratchpad);
				this.scratchpadsCount.incrementAndGet();
			} catch(ScratchpadException | SQLException e)
			{
				LOG.error("failed to create scratchpad pool", e);
				RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
			}

		}
	}

}
