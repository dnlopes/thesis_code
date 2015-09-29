package database.execution.temporary.pad;


import runtime.Transaction;

import java.sql.SQLException;


/**
 *
 * Created by dnlopes on 25/09/15.
 * A more complete version of the ReadOnlyScratchpad
 * Besides query statements, it allows for update and insert sql statements
 *
 */
public interface ReadWriteScratchpad extends ReadOnlyScratchpad
{
	public void startTransaction(Transaction txn) throws SQLException;
	public int getScratchpadId();
	public Transaction getActiveTransaction();
	public void resetScratchpad() throws SQLException;
	public int executeUpdate(String op) throws SQLException;
}
