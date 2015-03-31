package network.proxy;


import database.jdbc.Result;
import database.scratchpad.DBExecuteScratchpad;
import database.scratchpad.IDBScratchpad;
import database.scratchpad.ScratchpadException;
import network.AbstractNode;
import org.perf4j.StopWatch;
import runtime.RuntimeHelper;
import util.IDFactories.IdentifierFactory;

import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import runtime.txn.TransactionIdentifier;
import runtime.operation.DBSingleOperation;
import util.ObjectPool;

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

	private ObjectPool<IDBScratchpad> scratchpadsPool;
	private ConcurrentHashMap<TransactionIdentifier, IDBScratchpad> activeScratchpads;
	private IProxyNetwork networkInterface;
	private AtomicInteger transactionsCounter;
	private AtomicInteger scratchpadsCount;

	public Proxy(ProxyConfig config)
	{
		super(config);

		this.scratchpadsPool = new ObjectPool<>();
		this.activeScratchpads = new ConcurrentHashMap<>();
		this.networkInterface = new ProxyNetwork(this.getConfig());
		this.transactionsCounter = new AtomicInteger();
		this.scratchpadsCount = new AtomicInteger();

		IdentifierFactory.createGenerators(this.getConfig());

		this.setup();

		LOG.info("proxy {} online.", this.config.getId());
	}

	@Override
	public ProxyConfig getConfig()
	{
		return (ProxyConfig) this.config;
	}

	public ResultSet executeQuery(DBSingleOperation op, TransactionIdentifier txnId)
			throws SQLException, ScratchpadException, JSQLParserException
	{

		return this.activeScratchpads.get(txnId).executeQuery(op);
	}

	public Result executeUpdate(DBSingleOperation op, TransactionIdentifier txnId)
			throws JSQLParserException, SQLException, ScratchpadException
	{
		IDBScratchpad pad = this.activeScratchpads.get(txnId);
		pad.setNotReadOnly();
		return this.activeScratchpads.get(txnId).executeUpdate(op);
	}

	public boolean commit(TransactionIdentifier txnId)
	{
		IDBScratchpad pad = this.activeScratchpads.get(txnId);

		/* if does not contain the txn, it means the transaction was not yet created
		 i.e no statements were executed. Thus, it should commit in every case */
		if(pad == null)
			return true;

		//TODO: maybe assign logical clocks and timestamp here?
		boolean commitResult = pad.commitTransaction(this.networkInterface);

		this.activeScratchpads.remove(txnId);
		// TODO: should we clean pad at this point?
		// for now we clean at transaction startup
		this.scratchpadsPool.returnObject(pad);
		txnId.resetValue();

		return commitResult;
	}

	public void abort(TransactionIdentifier txnId)
	{
		IDBScratchpad pad = this.activeScratchpads.get(txnId);

		if(pad == null)
			return;

		this.activeScratchpads.remove(txnId);
		this.scratchpadsPool.returnObject(pad);
		txnId.resetValue();
	}

	public void closeTransaction(TransactionIdentifier txnId)
	{
		IDBScratchpad pad = this.activeScratchpads.get(txnId);

		if(pad == null)
			return;

		if(!pad.isReadOnly())
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

		IDBScratchpad pad;
		pad = this.scratchpadsPool.borrowObject();

		if(pad == null)
		{
			LOG.warn("scratchpad pool was empty");
			try
			{
				pad = new DBExecuteScratchpad(this.scratchpadsCount.incrementAndGet(), this.getConfig());
			} catch(SQLException | ScratchpadException e)
			{
				LOG.error("failed to init scratchpad", e);
				RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
			}
		}

		this.activeScratchpads.put(txnId, pad);
		pad.startTransaction(txnId);
	}

	private void setup()
	{
		StopWatch watch = new StopWatch();
		watch.start();

		for(int i = 1; i <= this.getConfig().getScratchPadPoolSize(); i++)
		{
			try
			{
				IDBScratchpad scratchpad = new DBExecuteScratchpad(i, this.getConfig());
				this.scratchpadsPool.addObject(scratchpad);
				this.scratchpadsCount.incrementAndGet();
			} catch(ScratchpadException | SQLException e)
			{
				LOG.error("failed to create scratchpad pool", e);
				RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_INIT_FAILED);
			}

		}
		watch.stop();
		LOG.info("{} scratchpads created in {} ms", this.getConfig().getScratchPadPoolSize(), watch.getElapsedTime());
	}

}
