package database.execution.temporary;


import nodes.proxy.IProxyNetwork;
import runtime.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 * * Interface to execute transactions in a (temporary) sandboxed environment (occ implementation)
 */
public interface Scratchpad
{

	public void startTransaction(Transaction txn) throws SQLException;

	public void commitTransaction(IProxyNetwork proxy) throws SQLException;

	public int getScratchpadId();

	public Transaction getActiveTransaction();

	public ResultSet executeQuery(String op) throws SQLException;

	public ResultSet executeQueryMainStorage(String op) throws SQLException;

	public int executeUpdate(String op) throws SQLException;

	public int executeUpdateMainStorage(String op) throws SQLException;

	public void addToBatchUpdate(String op) throws SQLException;

	public int executeBatch() throws SQLException;

	public void resetScratchpad() throws SQLException;

}
