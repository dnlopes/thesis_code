package database.occ;

import database.jdbc.Result;
import database.occ.scratchpad.IDBScratchpad;
import database.occ.scratchpad.ScratchpadException;
import runtime.Operation;
import util.LogicalClock;
import util.TimeStamp;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Interface for defining the execution and occ policy for a given table
 *
 * @author nmp
 */
public interface IExecutor
{

	/**
	 * Called on begin transaction
	 */
	public void beginTx(IDBScratchpad db);
	/**
	 * Returns an unitialized fresh copy of this execution policy
	 */
	public IExecutor duplicate();
	/**
	 * Returns true if it is a blue table
	 */
	public boolean isBlue();

	/**
	 * Returns the table definition for this execution policy
	 */
	TableDefinition getTableDefinition();
	/**
	 * Returns the alias table name
	 */
	String getAliasTable();
	/**
	 * Returns the table name
	 */
	String getTableName();
	/**
	 * Add deleted to where statement
	 */
	void addDeletedKeysWhere(StringBuffer buffer);
	/**
	 * Returns what should be in the from clause in select statements
	 */
	void addFromTable(StringBuffer buffer, boolean both, String[] tableNames);
	/**
	 * Returns what should be in the from clause in select statements plus the primary key value for performance
	 */
	void addFromTablePlusPrimaryKeyValues(StringBuffer buffer, boolean both, String[] tableNames,
										  String whereClauseStr);
	/**
	 * Returns the text for retrieving key and version vector in select statements
	 */
	public void addKeyVVBothTable(StringBuffer buffer, String tableAlias);
	/**
	 * Called on scratchpad initialization for a given table. Allows to setup any internal state needed
	 */
	void setup(DatabaseMetaData metadata, String tableName, int tableId, IDBScratchpad scratchpad);

	/**
	 * Executes a query in the scratchpad temporary state.
	 *
	 * @throws database.occ.scratchpad.ScratchpadException
	 */
	Result executeTemporaryQuery(Operation dbOp, IDBScratchpad db, String[] table)
			throws SQLException, ScratchpadException;

	/**
	 * Executes a query in the scratchpad temporary state for a query that combines multiple ExecutionPolicies.
	 *
	 * @throws database.occ.scratchpad.ScratchpadException
	 */
	Result executeTemporaryQuery(Operation dbOp, IDBScratchpad db, IExecutor[] policies, String[][] table)
			throws SQLException, ScratchpadException;

	/**
	 * Executes a query against database with temporary state for a single table
	 */
	ResultSet executeTemporaryQueryOrig(Operation dbOp, IDBScratchpad db, String[] table) throws SQLException;
	/**
	 * Executes a query against database with temporary state for multiple table policies
	 *
	 * @throws java.sql.SQLException
	 */

	ResultSet executeTemporaryQueryOrig(Operation dbOp, IDBScratchpad db, IExecutor[] policies, String[][] table)
			throws SQLException;

	/**
	 * Executes an update in the scratchpad temporary state.
	 *
	 * @throws database.occ.scratchpad.ScratchpadException
	 */
	Result executeTemporaryUpdate(Operation dbOp, IDBScratchpad db) throws SQLException, ScratchpadException;

	/**
	 * Executes an update in the scratchpad final state.
	 *
	 * @param b
	 */
	void executeDefiniteUpdate(Operation dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b)
			throws SQLException;
	/**
	 * Executes an update in the scratchpad final state for generic operation.
	 *
	 * @param b
	 */
	Result executeDefiniteUpdate1(Operation dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b)
			throws SQLException;

	//update timestamp only
	Result executeOnlyOp(Operation op, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * not create temporary table, but set table meta data
	 *
	 * @param dm
	 * @param tableName
	 * @param id
	 * @param tableId
	 *
	 * @throws database.occ.scratchpad.ScratchpadException
	 */
	void init(DatabaseMetaData dm, String tableName, int id, int tableId) throws ScratchpadException;

}
