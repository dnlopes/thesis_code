package network;

import database.jdbc.Result;
import database.occ.scratchpad.ExecutePadFactory;
import database.occ.scratchpad.IDBScratchpad;
import database.occ.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Transaction;
import runtime.factory.TxnIdFactory;
import runtime.operation.DBSingleOperation;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Proxy extends Node
{

	static final Logger LOG = LoggerFactory.getLogger(Proxy.class);

	private Transaction transaction;
	private IDBScratchpad pad;

	public Proxy(String hostName, int port, int id)
	{
		super(hostName, port, id, Role.PROXY);
		this.transaction = new Transaction();
		this.pad = ExecutePadFactory.getScratchpad();
	}

	public void beginTxn()
	{
		long txnId = TxnIdFactory.getNextId();
		this.transaction.beginTxn(txnId);
		LOG.info("Beggining txn {}", txnId);
	}

	public boolean txnHasBegun()
	{
		return this.transaction.hasBegun();
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		return pad.executeQuery(sql);
	}

	public Transaction getTransaction()
	{
		return this.transaction;
	}

	public Result execute(DBSingleOperation op, long txnId)
			throws JSQLParserException, SQLException, ScratchpadException
	{
		return pad.execute(op, txnId);
	}

	public boolean commit()
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

		this.transaction.endTxn();
		LOG.info("committing txn {}", this.transaction.getTxnId());
		return true;
	}

	public void abortTransaction()
	{
		//TODO
		LOG.info("aborting txn {}", this.transaction.getTxnId());
	}
}
