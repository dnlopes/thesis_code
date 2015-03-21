package network.node;


import database.jdbc.Result;
import database.occ.scratchpad.ExecutePadFactory;
import database.occ.scratchpad.IDBScratchpad;
import database.occ.scratchpad.ScratchpadException;
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
		// if does not contain the txn, it means the transaction was not yet created
		// i.e no statements were executed. Thus, it should commit in every case
		if(!this.transactions.containsKey(txnId))
			return true;

		Transaction txn = this.transactions.get(txnId);

		// FIXME: this call MUST NOT block, but for now it DOES block
		boolean commitDecision = this.networkInterface.commitOperation(txn.getShadowOp(), this.replicator);

		if(commitDecision)
		{
			txn.endTxn();
			LOG.trace("txn {} committed", txn.getTxnId().getId());
		} else
			LOG.trace("txn {} failed to commit", txn.getTxnId().getId());

		return commitDecision;
	}

	/**
	 * Aborts the transaction at application request.
	 * It is called when the connection decides to rollback
	 *
	 * @param txnId
	 * 		the id of the transaction to abort
	 */
	public void abortTransaction(TransactionId txnId)
	{
		//TODO
		Transaction txn = this.transactions.get(txnId);
		LOG.trace("aborting txn {}", txn.getTxnId());
	}

	public Transaction getTransaction(TransactionId txnId)
	{
		return this.transactions.get(txnId);
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

	public boolean txnHasBegun(TransactionId txnId)
	{
		return this.transactions.containsKey(txnId);
	}
}
