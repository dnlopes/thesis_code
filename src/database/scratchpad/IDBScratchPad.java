package database.scratchpad;


import database.jdbc.Result;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import network.proxy.IProxyNetwork;
import runtime.operation.DBSingleOperation;
import runtime.txn.Transaction;
import runtime.txn.TransactionIdentifier;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public interface IDBScratchPad
{

	public void startTransaction(TransactionIdentifier txnId);

	public boolean commitTransaction(IProxyNetwork proxy);

	public int getScratchpadId();

	public Transaction getActiveTransaction();

	public Result executeUpdate(DBSingleOperation op) throws SQLException;

	public ResultSet executeQuery(DBSingleOperation op) throws SQLException;

	public void addToBatchUpdate(String op) throws SQLException;

	public int executeBatch() throws SQLException;

	public ResultSet executeQuery(String op) throws SQLException;

	public int executeUpdate(String op) throws SQLException;
}
