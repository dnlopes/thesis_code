package weaql.client.execution.temporary.scratchpad.agent;


import weaql.client.execution.QueryCreator;
import weaql.client.execution.TransactionContext;
import weaql.client.operation.*;
import weaql.client.execution.temporary.scratchpad.IDBScratchpad;
import weaql.client.execution.temporary.scratchpad.ScratchpadException;
import weaql.common.database.Record;
import weaql.common.database.SQLInterface;
import weaql.common.database.constraints.fk.ForeignKeyConstraint;
import weaql.common.database.field.DataField;
import weaql.common.database.table.DatabaseTable;
import weaql.common.database.util.DatabaseCommon;
import weaql.common.database.util.PrimaryKeyValue;
import weaql.common.database.util.Row;
import org.apache.commons.dbutils.DbUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 04/12/15.
 */
public class ExecutorAgent extends AbstractExecAgent implements IExecutorAgent
{

	private ExecutionHelper helper;

	public ExecutorAgent(int scratchpadId, int tableId, String tableName, SQLInterface sqlInterface, IDBScratchpad pad,
						 TransactionContext txnRecord) throws SQLException
	{
		super(scratchpadId, tableId, tableName, sqlInterface, pad, txnRecord);

		this.helper = new ExecutionHelper();
	}

	@Override
	public ResultSet executeTemporaryQuery(SQLSelect selectOp) throws ScratchpadException
	{
		long start = System.nanoTime();
		//TODO filter UPDATED ROWS properly
		selectOp.prepareOperation(tempTableName);

		ResultSet rs;
		try
		{
			rs = this.sqlInterface.executeQuery(selectOp.getSQLString());
		} catch(SQLException e)
		{
			throw new ScratchpadException(e.getMessage());
		}

		long estimated = System.nanoTime() - start;
		this.txnRecord.addSelectTime(estimated);
		return rs;
	}

	@Override
	public int executeTemporaryUpdate(SQLWriteOperation sqlOp) throws ScratchpadException
	{
		if(sqlOp.getOpType() == SQLOperationType.DELETE)
		{
			return executeTempOpDelete((SQLDelete) sqlOp);
		} else
		{
			if(sqlOp.getOpType() == SQLOperationType.INSERT)
			{
				isDirty = true;
				return executeTempOpInsert((SQLInsert) sqlOp);
			} else if(sqlOp.getOpType() == SQLOperationType.UPDATE)
			{
				isDirty = true;
				return executeTempOpUpdate((SQLUpdate) sqlOp);
			} else
				throw new ScratchpadException("update statement not found");
		}
	}

	@Override
	public void scanTemporaryTables(List<Record> recordsList) throws ScratchpadException
	{
		StringBuilder buffer = new StringBuilder(FULL_SCAN_PREFIX);
		buffer.append(tempTableName);

		String sqlQuery = buffer.toString();

		ResultSet rs;
		try
		{
			rs = sqlInterface.executeQuery(sqlQuery);

			while(rs.next())
			{
				Record aRecord = DatabaseCommon.loadRecordFromResultSet(rs, databaseTable);
				recordsList.add(aRecord);
			}
		} catch(SQLException e)
		{
			throw new ScratchpadException(e.getMessage());
		}

	}

	private int executeTempOpInsert(SQLInsert insertOp) throws ScratchpadException
	{
		try
		{
			long start = System.nanoTime();

			Record toInsertRecord = insertOp.getRecord();
			scratchpad.getWriteSet().addToInserts(toInsertRecord);
			scratchpad.getWriteSet().addToCache(toInsertRecord);

			insertOp.prepareOperation(false, this.tempTableName);

			int result = this.sqlInterface.executeUpdate(insertOp.getSQLString());
			long estimated = System.nanoTime() - start;
			this.txnRecord.addInsertTime(estimated);
			return result;
		} catch(SQLException e)
		{
			throw new ScratchpadException(e.getMessage());
		}
	}

	private int executeTempOpDelete(SQLDelete deleteOp) throws ScratchpadException
	{
		Record toDeleteRecord = deleteOp.getRecord();

		if(!toDeleteRecord.isPrimaryKeyReady())
			throw new ScratchpadException("pk value missing for this delete query");

		try
		{
			long start = System.nanoTime();
			StringBuilder buffer = new StringBuilder();
			buffer.append("DELETE FROM ").append(tempTableName).append(" WHERE ");
			buffer.append(deleteOp.getRecord().getPkValue());
			String delete = buffer.toString();

			int result = this.sqlInterface.executeUpdate(delete);
			long estimated = System.nanoTime() - start;
			txnRecord.addDeleteTime(estimated);
			scratchpad.getWriteSet().addToDeletes(toDeleteRecord);

			return result;
		} catch(SQLException e)
		{
			throw new ScratchpadException(e.getMessage());
		}
	}

	private int executeTempOpUpdate(SQLUpdate updateOp) throws ScratchpadException
	{
		long loadingFromMain;
		long execUpdate;

		Record cachedRecord = updateOp.getCachedRecord();

		if(!cachedRecord.isPrimaryKeyReady())
			throw new ScratchpadException("cached record is missing pk value");

		// if NOT in cache, we have to retrieved it from main storage
		// previously inserted records go to cache as well
		if(!scratchpad.getWriteSet().getCachedRecords().containsKey(cachedRecord.getPkValue().getUniqueValue()))
		{
			long start = System.nanoTime();
			this.helper.addMissingRowsToScratchpad(updateOp);
			loadingFromMain = System.nanoTime() - start;
			txnRecord.addLoadfromMainTime(loadingFromMain);
		}

		try
		{
			long start = System.nanoTime();
			updateOp.prepareOperation(true, this.tempTableName);
			int result = this.sqlInterface.executeUpdate(updateOp.getSQLString());
			execUpdate = System.nanoTime() - start;
			txnRecord.addUpdateTime(execUpdate);
			scratchpad.getWriteSet().addToUpdates(updateOp.getRecord());

			return result;
		} catch(SQLException e)
		{
			throw new ScratchpadException(e.getMessage());
		}
	}

	private class ExecutionHelper
	{

		public static final String WHERE = " WHERE (";
		public static final String AND = " AND (";

		/**
		 * Inserts missing rows in the temporary table and returns the list of rows
		 * This must be done before updating rows in the scratchpad.
		 *
		 * @param updateOp
		 * @param pad
		 *
		 * @throws SQLException
		 */
		private void addMissingRowsToScratchpad(SQLUpdate updateOp) throws ScratchpadException
		{
			//TODO (optimization)
			// if we have already loaded the record previously (during parsing time)
			// we do not need to do the select here, we just need to insert in temp tables
			// this can happen when the original update is not specified by the PK
			// in such case, we perform a select PK to know which records will be updated

			StringBuilder buffer = new StringBuilder("SELECT ");
			buffer.append(updateOp.getDbTable().getNormalFieldsSelection());
			buffer.append(" FROM ").append(updateOp.getDbTable().getName());
			buffer.append(" WHERE ").append(updateOp.getCachedRecord().getPkValue().getPrimaryKeyWhereClause());

			String sqlQuery = buffer.toString();

			ResultSet rs;

			try
			{
				rs = sqlInterface.executeQuery(sqlQuery);
				while(rs.next())
				{
					if(!rs.isLast())
						throw new ScratchpadException("expected only one record, but instead we got more");

					buffer.setLength(0);
					buffer.append("INSERT INTO ");
					buffer.append(tempTableName);
					buffer.append(" (");
					StringBuilder valuesBuffer = new StringBuilder(" VALUES (");

					Iterator<DataField> fieldsIt = fields.values().iterator();
					Record cachedRecord = updateOp.getCachedRecord();

					while(fieldsIt.hasNext())
					{
						DataField field = fieldsIt.next();

						buffer.append(field.getFieldName());
						if(fieldsIt.hasNext())
							buffer.append(",");

						String cachedContent = rs.getString(field.getFieldName());

						if(cachedContent == null)
							cachedContent = "NULL";

						cachedRecord.addData(field.getFieldName(), cachedContent);
						valuesBuffer.append(field.formatValue(cachedContent));

						if(fieldsIt.hasNext())
							valuesBuffer.append(",");
					}

					if(!cachedRecord.isFullyCached())
						throw new SQLException("failed to retrieve full record from main storage");

					scratchpad.getWriteSet().addToCache(cachedRecord);

					buffer.append(")");
					buffer.append(valuesBuffer.toString());
					buffer.append(")");
					sqlInterface.executeUpdate(buffer.toString());
				}

			} catch(SQLException e)
			{
				throw new ScratchpadException(e.getMessage());
			}
		}

		private Map<String, String> findParentRows(Row childRow, List<ForeignKeyConstraint> constraints,
												   SQLInterface sqlInterface) throws SQLException
		{
			Map<String, String> parentByConstraint = new HashMap<>();

			for(int i = 0; i < constraints.size(); i++)
			{
				ForeignKeyConstraint c = constraints.get(i);

				if(!c.getParentTable().getTablePolicy().allowDeletes())
					continue;

				Row parent = findParent(childRow, c, sqlInterface);
				parentByConstraint.put(c.getConstraintIdentifier(),
						parent.getPrimaryKeyValue().getPrimaryKeyWhereClause());

				if(parent == null)
					throw new SQLException("parent row not found. Foreing key violated");
			}

			//return null in the case where app never deletes any parent
			if(parentByConstraint.size() == 0)
				return null;
			else
				return parentByConstraint;
		}

		private Row findParent(Row childRow, ForeignKeyConstraint constraint, SQLInterface sqlInterface)
				throws SQLException
		{
			String query = QueryCreator.findParent(childRow, constraint, scratchpadId);

			ResultSet rs = sqlInterface.executeQuery(query);
			if(!rs.isBeforeFirst())
			{
				DbUtils.closeQuietly(rs);
				throw new SQLException("parent row not found. Foreing key violated");
			}

			rs.next();
			DatabaseTable remoteTable = constraint.getParentTable();
			PrimaryKeyValue parentPk = DatabaseCommon.getPrimaryKeyValue(rs, remoteTable);
			DbUtils.closeQuietly(rs);

			return new Row(remoteTable, parentPk);
		}
	}

}
