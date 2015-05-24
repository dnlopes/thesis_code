package database.scratchpad;


import nodes.proxy.IProxyNetwork;
import runtime.operation.DBSingleOperation;
import runtime.txn.Transaction;
import runtime.txn.TransactionIdentifier;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 * * Interface to execute transactions in a sandboxed environment (occ implementation)
 */
public interface IDBScratchPad
{

	public void startTransaction(TransactionIdentifier txnId);

	public boolean commitTransaction(IProxyNetwork proxy);

	public int getScratchpadId();

	public Transaction getActiveTransaction();

	public int executeUpdate(DBSingleOperation op) throws SQLException;

	public ResultSet executeQuery(DBSingleOperation op) throws SQLException;

	public void addToBatchUpdate(String op) throws SQLException;

	public int executeBatch() throws SQLException;

	public ResultSet executeQuery(String op) throws SQLException;

	public int executeUpdate(String op) throws SQLException;
}
