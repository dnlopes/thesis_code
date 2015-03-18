package network;


import database.jdbc.Result;
import database.occ.scratchpad.ExecutePadFactory;
import database.occ.scratchpad.IDBScratchpad;
import database.occ.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
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
public class Proxy extends Node
{

	static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

	// records all active transactions along with their respective scratchpads
	private Map<TransactionId, Transaction> transactions;
	private Map<TransactionId, IDBScratchpad> pads;

	public Proxy(String hostName, int port, int id)
	{
		super(hostName, port, id, Role.PROXY);
		this.transactions = new HashMap<>();
		this.pads = new HashMap<>();
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

	public ResultSet executeQuery(TransactionId txnId, String sql) throws SQLException
	{

		return this.pads.get(txnId).executeQuery(sql);
	}

	public Transaction getTransaction(TransactionId txnId)
	{
		return this.transactions.get(txnId);
	}

	public Result execute(DBSingleOperation op, TransactionId txnId)
			throws JSQLParserException, SQLException, ScratchpadException
	{
		return this.pads.get(txnId).execute(op);
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
		//TODO  this method must block until receive ack from replicator

		// we can block on a condition variable of each transaction
		// for this the network interface must have the txn object to change the value
		/**
		 * 1- wrap shadowOperation
		 * 2- send to my replicator
		 * 3- wait for decision
		 * 4- clean this connection state
		 * 4- respond to client
		 * */
		Transaction txn = this.transactions.get(txnId);

		txn.endTxn();
		LOG.trace("committing txn {}", txn.getTxnId());
		return true;
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
}
