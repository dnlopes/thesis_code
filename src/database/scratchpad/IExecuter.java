package database.scratchpad;


import net.sf.jsqlparser.statement.select.Select;
import runtime.operation.DBSingleOperation;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Interface for defining the execution and occ policy for a given table
 *
 * @author nmp
 */
public interface IExecuter
{

	/**
	 * Returns the table definition for this execution policy
	 */
	public TableDefinition getTableDefinition();

	/**
	 * Returns the alias table name
	 */
	public String getAliasTable();

	/**
	 * Returns the table name
	 */
	public String getTableName();

	/**
	 * Add deleted to where statement
	 */
	public void addDeletedKeysWhere(StringBuilder buffer);

	/**
	 * Returns what should be in the from clause in select statements plus the primary key value for performance
	 */
	public void addFromTablePlusPrimaryKeyValues(StringBuilder buffer, boolean both, String[] tableNames,
										  String whereClauseStr);

	/**
	 * Returns the text for retrieving key and version vector in select statements
	 */
	public void addKeyVVBothTable(StringBuilder buffer, String tableAlias);

	/**
	 * Called on scratchpad initialization for a given table. Allows to setup any internal state needed
	 */
	public void setup(DatabaseMetaData metadata, IDBScratchPad scratchpad);

	/**
	 * Executes a query in the scratchpad temporary state.
	 *
	 * @throws ScratchpadException
	 */
	public ResultSet executeTemporaryQueryOnSingleTable(Select selectOp, IDBScratchPad db)
			throws SQLException, ScratchpadException;

	/**
	 * Executes a query in the scratchpad temporary state for a query that combines multiple ExecutionPolicies.
	 *
	 * @throws ScratchpadException
	 */
	public ResultSet executeTemporaryQueryOnMultTable(Select selectOp, IDBScratchPad db, IExecuter[] policies,
											String[][] table) throws SQLException, ScratchpadException;

	/**
	 * Executes an update in the scratchpad temporary state.
	 *
	 * @throws ScratchpadException
	 */
	public int executeTemporaryUpdate(DBSingleOperation dbOp, IDBScratchPad db) throws SQLException,
			ScratchpadException;

	/**
	 * Cleans the state of the temporary table of this executer
	 */
	public void resetExecuter(IDBScratchPad pad) throws SQLException;

}
