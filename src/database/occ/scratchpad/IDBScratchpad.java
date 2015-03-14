package database.occ.scratchpad;

import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBWriteSetEntry;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 10/03/15.
 */
public interface IDBScratchpad
{

	public int getScratchpadId();

	/**
	 * Returns true if current transaction is read-only.
	 */
	public boolean isReadOnly();

	/**
	 * Executes a query in the scratchpad state.
	 */
	public ResultSet executeQuery(String op) throws SQLException;

	/**
	 * Executes an update in the scratchpad state.
	 */
	public int executeUpdate(String op) throws SQLException;

	/**
	 * Add an update to the batch in the scratchpad state.
	 */
	public void addToBatchUpdate(String op) throws SQLException;
	/**
	 * Execute operations in the batch so far
	 */
	public int executeBatch() throws SQLException;

	/**
	 * Clears the state of this pad. Should be called before releasing the pad
	 */
	public void cleanState();

	/**
	 * Add the given entry to the write set
	 */
	boolean addToWriteSet(DBWriteSetEntry entry);
	/**
	 * Add the given entry to the read set
	 */
	boolean addToReadSet(DBReadSetEntry readSetEntry);

}
