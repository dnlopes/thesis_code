package network.proxy;


import database.jdbc.Result;
import database.scratchpad.ScratchpadFactory;
import database.scratchpad.IDBScratchpad;
import database.scratchpad.ScratchpadException;
import network.AbstractNode;
import network.coordinator.CoordinatorConfig;
import network.replicator.ReplicatorConfig;
import org.apache.thrift.TException;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import runtime.RuntimeHelper;
import runtime.txn.TransactionWriteSet;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import runtime.txn.Transaction;
import runtime.txn.TransactionId;
import runtime.factory.TxnIdFactory;
import runtime.operation.DBSingleOperation;
import runtime.operation.ShadowOperation;
import util.thrift.ThriftCheckEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Proxy extends AbstractNode
{

	private static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

	private IProxyNetwork networkInterface;
	//this is the replicator in which we will execute RPCs
	private ReplicatorConfig replicator;
	private CoordinatorConfig coordinator;

	// records all active transactions along with their respective scratchpads
	private Map<TransactionId, Transaction> transactions;
	private Map<TransactionId, IDBScratchpad> scratchpad;

	public Proxy(ProxyConfig config) throws TTransportException
	{
		super(config);

		this.transactions = new HashMap<>();
		this.scratchpad = new HashMap<>();
		this.networkInterface = new ProxyNetwork(this.getConfig());
		this.replicator = config.getReplicatorConfig();
		this.coordinator = config.getCoordinatorConfig();
	}

	@Override
	public ProxyConfig getConfig()
	{
		return (ProxyConfig) this.config;
	}

	public ResultSet executeQuery(DBSingleOperation op, TransactionId txnId)
			throws SQLException, ScratchpadException, JSQLParserException
	{

		return this.scratchpad.get(txnId).executeQuery(op);
	}

	public Result executeUpdate(DBSingleOperation op, TransactionId txnId)
			throws JSQLParserException, SQLException, ScratchpadException
	{

		return this.scratchpad.get(txnId).executeUpdate(op);
	}

	/**
	 * Atempts to commit a transaction.
	 * This method does not actually commit the transaction.
	 * Instead, it sends the shadow operation to the replicator and waits for the acknowledge
	 *
	 * @param txnId
	 * 		the id of the transaction to commit
	 *
	 * @return - the commit decision
	 */
	public boolean commit(TransactionId txnId)
	{
		StopWatch watch = new LoggingStopWatch("commit time");
		/* if does not contain the txn, it means the transaction was not yet created
		 i.e no statements were executed. Thus, it should commit in every case */
		if(!this.transactions.containsKey(txnId))
			return true;

		Transaction txn = this.transactions.get(txnId);

		if(txn.isReadOnly())
		{
			txn.finish();
			LOG.info("txn {} committed in {} ms", txnId.getId(), txn.getLatency());
			this.resetTransactionInfo(txnId);
			return true;
		}

		this.prepareToCommit(txnId);

		if(!txn.isReadyToCommit())
		{
			// something went wrong
			// commit fails
			this.resetTransactionInfo(txnId);
			return false;
		}

		// FIXME: this call MUST NOT block, but for now it DOES
		boolean commitDecision = this.networkInterface.commitOperation(txn.getShadowOp(), this.replicator);

		if(commitDecision)
		{
			txn.finish();
			LOG.info("txn {} committed in {} ms", txn.getTxnId().getId(), txn.getLatency());
		} else
			LOG.error("txn {} failed to commit", txn.getTxnId().getId());

		watch.start();
		this.resetTransactionInfo(txnId);
		watch.stop();
		return commitDecision;
	}

	public void beginTransaction(Transaction txn)
	{
		long txnId = TxnIdFactory.getNextId();
		IDBScratchpad pad = ScratchpadFactory.getInstante().getScratchpad();

		this.transactions.put(txn.getTxnId(), txn);
		this.scratchpad.put(txn.getTxnId(), pad);
		txn.start(txnId);

		LOG.info("Beggining txn {}", txnId);
	}

	public void resetTransactionInfo(TransactionId txn)
	{
		Transaction transaction = this.transactions.get(txn);
		transaction.resetState();
		IDBScratchpad pad = this.scratchpad.get(txn);
		try
		{
			pad.resetScratchpad();
		} catch(SQLException e)
		{
			LOG.error("failed to clean scratchpad state {}", pad.getScratchpadId(), e);
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_CLEANUP_ERROR);
		}

		this.transactions.remove(txn);
		this.scratchpad.remove(txn);

		LOG.trace("scratchpad {} cleaned", pad.getScratchpadId());
		ScratchpadFactory.getInstante().releaseScratchpad(pad);
	}

	private void prepareToCommit(TransactionId txnId)
	{
		StopWatch watch = new LoggingStopWatch("preparing to commit time");
		watch.start();
		LOG.trace("preparing to commit txn {}", txnId.getId());

		IDBScratchpad pad = this.scratchpad.get(txnId);
		try
		{
			TransactionWriteSet writeSet = pad.createTransactionWriteSet();
			List<ThriftCheckEntry> checkList = writeSet.verifyInvariants();

			//if we must coordinate then do it here. this is a blocking call
			if(checkList.size() > 0)
				this.networkInterface.checkInvariants(checkList, this.coordinator);

			writeSet.generateMinimalStatements();
			ShadowOperation shadowOp = new ShadowOperation(txnId.getId(), writeSet.getStatements());
			this.transactions.get(txnId).setReadyToCommit(shadowOp);

		} catch(SQLException | TException e)
		{
			LOG.error("failed to prepare operation {} for commit", txnId.getId(), e);
		}

		watch.stop();
	}

}