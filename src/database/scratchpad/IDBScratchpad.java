package database.scratchpad;


import database.jdbc.Result;
import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import network.proxy.IProxyNetwork;
import runtime.operation.DBSingleOperation;
import runtime.txn.TransactionIdentifier;
import runtime.txn.TransactionWriteSet;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public interface IDBScratchpad
{

	public void startTransaction(TransactionIdentifier txnId);

	public boolean commitTransaction(IProxyNetwork proxy);

	public int getScratchpadId();

	public boolean isReadOnly();

	public void setNotReadOnly();

	public Result executeUpdate(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException;

	public ResultSet executeQuery(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException;

	public void addToBatchUpdate(String op) throws SQLException;

	public int executeBatch() throws SQLException;

	public void abort() throws SQLException;

	boolean addToWriteSet(DBWriteSetEntry entry);

	boolean addToReadSet(DBReadSetEntry readSetEntry);

	public ResultSet executeQuery(String op) throws SQLException;

	public int executeUpdate(String op) throws SQLException;

	public void resetScratchpad() throws SQLException;

	public TransactionWriteSet createTransactionWriteSet() throws SQLException;
}
