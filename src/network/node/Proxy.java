package network.node;


import database.jdbc.Result;
import database.scratchpad.ExecutePadFactory;
import database.scratchpad.IDBScratchpad;
import database.scratchpad.ScratchpadException;
import database.scratchpad.TransactionWriteSet;
import net.sf.jsqlparser.JSQLParserException;
import network.IProxyNetwork;
import network.ProxyNetwork;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Transaction;
import runtime.TransactionId;
import runtime.factory.TxnIdFactory;
import runtime.operation.DBSingleOperation;
import runtime.operation.ShadowOperation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Proxy extends AbstractNode
{

	static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

	private IProxyNetwork networkInterface;
	//this is the replicator in which we will execute RPCs
	private Replicator replicator;
	// records all active transactions along with their respective scratchpads
	private Map<TransactionId, Transaction> transactions;
	private Map<TransactionId, IDBScratchpad> pads;

	public Proxy(String hostName, int port, int id, Replicator replicator) throws TTransportException
	{
		super(hostName, port, id, Role.PROXY);

		this.transactions = new HashMap<>();
		this.pads = new HashMap<>();
		this.replicator = replicator;
		this.networkInterface = new ProxyNetwork(this);
	}

	public ResultSet executeQuery(DBSingleOperation op, TransactionId txnId)
			throws SQLException, ScratchpadException, JSQLParserException
	{

		return this.pads.get(txnId).executeQuery(op);
	}

	public Result executeUpdate(DBSingleOperation op, TransactionId txnId)
			throws JSQLParserException, SQLException, ScratchpadException
	{

		return this.pads.get(txnId).executeUpdate(op);
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
		/* if does not contain the txn, it means the transaction was not yet created
		 i.e no statements were executed. Thus, it should commit in every case */
		if(!this.transactions.containsKey(txnId))
			return true;

		Transaction txn = this.transactions.get(txnId);

		if(txn.isReadOnly())
			return true;

		this.prepareToCommit(txnId);
		/* TODO: PREPARE OPERATION
		// 1- ask coodinator for values
		2- prepare sql operations based on WriteSets
		*/

		if(!txn.isReadyToCommit())
		{
			this.resetTransactionInfo(txnId);
			return false;
		}

		// FIXME: this call MUST NOT block, but for now it DOES block
		boolean commitDecision = this.networkInterface.commitOperation(txn.getShadowOp(), this.replicator);

		if(commitDecision)
		{
			txn.endTxn();
			LOG.trace("txn {} committed", txn.getTxnId().getId());
		} else
			LOG.trace("txn {} failed to commit", txn.getTxnId().getId());

		this.resetTransactionInfo(txnId);
		return commitDecision;
	}

	public void beginTxn(Transaction txn)
	{
		long txnId = TxnIdFactory.getNextId();
		IDBScratchpad pad = ExecutePadFactory.getScratchpad();

		this.transactions.put(txn.getTxnId(), txn);
		this.pads.put(txn.getTxnId(), pad);
		txn.beginTxn(txnId);

		LOG.info("Beggining txn {}", txnId);
	}

	public void resetTransactionInfo(TransactionId txn)
	{
		Transaction transaction = this.transactions.get(txn);
		transaction.resetState();
		IDBScratchpad pad = this.pads.get(txn);
		try
		{
			pad.resetScratchpad();
		} catch(SQLException e)
		{
			LOG.warn("failed to clean scratchpad state {}", pad.getScratchpadId());
			e.printStackTrace();
		}
	}

	public IDBScratchpad getScratchpadOfTxn(TransactionId txnId)
	{
		return this.pads.get(txnId);
	}

	public void prepareToCommit(TransactionId txnId)
	{

		IDBScratchpad pad = this.pads.get(txnId);
		try
		{
			TransactionWriteSet writeSet = pad.createTransactionWriteSet();
			writeSet.generateMinimalStatements();
			ShadowOperation shadowOp = new ShadowOperation(writeSet.getStatements());
			this.transactions.get(txnId).setReadyToCommit(shadowOp);

		} catch(SQLException e)
		{
			LOG.error("failed to prepare operation for commit");
			e.printStackTrace();
		}

	}
}
