package client.execution.temporary.scratchpad.agent;


import client.execution.QueryCreator;
import client.execution.TransactionRecord;
import client.execution.operation.*;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import common.database.Record;
import common.database.SQLInterface;
import common.database.constraints.fk.ForeignKeyConstraint;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.util.*;
import common.database.value.FieldValue;
import common.thrift.*;
import common.util.ExitCode;
import common.util.RuntimeUtils;
import common.util.defaults.DatabaseDefaults;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import org.apache.commons.dbutils.DbUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 04/12/15.
 */
public class DBExecutorPerfAgent extends AbstractExecAgent implements IExecutorAgent
{

	private ExecutionHelper helper;
	private Set<PrimaryKeyValue> duplicatedRows;
	private Set<PrimaryKeyValue> deletedRows;
	private Map<String, PrimaryKeyValue> recordedPkValues;
	private Map<PrimaryKeyValue, Record> cachedRecords;
	private Map<PrimaryKeyValue, Record> newRecords;

	public DBExecutorPerfAgent(int scratchpadId, int tableId, String tableName, SQLInterface sqlInterface,
							   ReadWriteScratchpad pad, TransactionRecord txnRecord) throws SQLException
	{
		super(scratchpadId, tableId, tableName, sqlInterface, pad, txnRecord);

		this.duplicatedRows = new HashSet<>();
		this.deletedRows = new HashSet<>();
		this.recordedPkValues = new HashMap<>();
		this.helper = new ExecutionHelper();
		this.cachedRecords = new HashMap<>();
		this.newRecords = new HashMap<>();
	}

	@Override
	public ResultSet executeTemporaryQuery(Select selectOp) throws SQLException
	{
		long start = System.nanoTime();
		//TODO filter DELETED and UPDATED ROWS properly

		StringBuilder mainBuffer = new StringBuilder();
		StringBuilder buffer = new StringBuilder();

		ExpressionDeParser expressionDeParser = new ExpressionDeParser()
		{
			boolean done = false;

			@Override
			public void visit(AndExpression andExpression)
			{
				if(andExpression.isNot())
					getBuffer().append(" NOT ");

				andExpression.getLeftExpression().accept(this);
				getBuffer().append(" AND ");
				andExpression.getRightExpression().accept(this);

				if(!done)
				{
					getBuffer().append(" ").append(ExecutionHelper.NOT_DELETED_EXPRESSION);
					done = true;
				}
			}

			@Override
			public void visit(EqualsTo equalsTo)
			{
				visitOldOracleJoinBinaryExpression(equalsTo, " = ");

				if(!done)
				{
					getBuffer().append(" AND ").append(ExecutionHelper.NOT_DELETED_EXPRESSION);
					done = true;
				}
			}
		};

		SelectDeParser deparser = new SelectDeParser(expressionDeParser, buffer);
		expressionDeParser.setSelectVisitor(deparser);
		expressionDeParser.setBuffer(buffer);
		selectOp.getSelectBody().accept(deparser);

		mainBuffer.append("(");
		mainBuffer.append(buffer.toString()).append(") UNION (");

		PlainSelect plainSelect = ((PlainSelect) selectOp.getSelectBody());
		((Table) plainSelect.getFromItem()).setName(this.tempTableName);

		mainBuffer.append(selectOp.toString());
		mainBuffer.append(")");

		String finalQuery = mainBuffer.toString();
		ResultSet rs = this.sqlInterface.executeQuery(finalQuery);

		long estimated = System.nanoTime() - start;
		this.txnRecord.addSelectTime(estimated);
		return rs;
	}

	@Override
	public int executeTemporaryUpdate(SQLWriteOperation sqlOp) throws SQLException
	{
		if(sqlOp.getOpType() == SQLOperationType.DELETE)
		{
			return executeTempOpDelete((SQLDelete) sqlOp);
		} else
		{
			if(sqlOp.getOpType() == SQLOperationType.INSERT)
			{
				return executeTempOpInsert((SQLInsert) sqlOp);
			} else if(sqlOp.getOpType() == SQLOperationType.UPDATE)
			{
				return executeTempOpUpdate((SQLUpdate) sqlOp);
			} else
				throw new SQLException("update statement not found");
		}
	}

	@Override
	public void clearExecutor() throws SQLException
	{
		super.clearExecutor();
		this.duplicatedRows.clear();
		this.recordedPkValues.clear();
		this.deletedRows.clear();
		this.cachedRecords.clear();
		this.newRecords.clear();
	}

	public void prepareForCommit() throws SQLException
	{
		/*
		ResultSet rs = this.sqlInterface.executeQuery("SELECT * FROM " + this.tempTableName);
		WriteSet writeSet = new WriteSet();

		while(rs.next())
		{
			Record record = this.helper.loadRecordFromResultSet(rs);

			if(this.cachedRecords.containsKey(record.getPkValue()))
			{
				// its an updated record. Retrive LWW fields from temp table and
				// calculate delta values from the difference old/new values
				Record cachedRecord = this.cachedRecords.get(record.getPkValue());
				record.mergeRecords(cachedRecord);
				writeSet.addToUpdates(record);
				// in case of a check constraint exists, check with coordinator if we can safely apply the delta
			} else
			{
				//its a new record to be inserted
				// check uniques constraints and/or auto_increment fields
				writeSet.addToInserts(record);
			}
		}

		//TODO:
		// At some point - when inserting a child record - we must check the respective parent(s), to understand if we
		// can insert the child. Also, we must capture the foreign-key semantic here

		int a = 0;  */
	}

	private int executeTempOpInsert(SQLInsert insertOp) throws SQLException
	{
		long start = System.nanoTime();

		insertOp.prepareOperation(false, this.tempTableName);

		int result = this.sqlInterface.executeUpdate(insertOp.getSQLString());
		long estimated = System.nanoTime() - start;
		this.txnRecord.addInsertTime(estimated);

		return result;
	}

	private int executeTempOpDelete(SQLDelete deleteOp) throws SQLException
	{
		//if pkValue is set just delete from temp table and add the pkValue to the deletedSet for later filtering
		if(deleteOp.isPrimaryKeySet())
		{
			long start = System.nanoTime();
			deletedRows.add(deleteOp.getRecord().getPkValue());
			StringBuilder buffer = new StringBuilder();
			buffer.append("DELETE FROM ").append(tempTableName).append(" WHERE ");
			buffer.append(deleteOp.getRecord().getPkValue());
			String delete = buffer.toString();

			int result = this.sqlInterface.executeUpdate(delete);
			long estimated = System.nanoTime() - start;
			txnRecord.addDeleteTime(estimated);

			return result;
		} else
		{
			//TODO: review delete op
			CRDTOperation crdtOperation = new CRDTOperation();
			//crdtOperation.setOpId(transaction.getOpsListSize());

			StringBuilder buffer = new StringBuilder();
			buffer.append("SELECT ");
			buffer.append(this.databaseTable.getNormalFieldsSelection());
			buffer.append(" FROM ");
			buffer.append(deleteOp.getTable().toString());
			this.helper.buildWhereClause(buffer, deleteOp.getDelete().getWhere(), true, true);

			if(this.duplicatedRows.size() > 0 || this.deletedRows.size() > 0)
			{
				buffer.append("AND ");
				this.helper.generateNotInDeletedAndUpdatedClause(buffer);
				buffer.append(")");
			} else
				buffer.append(")");

			buffer.append(" UNION SELECT ");
			buffer.append(this.databaseTable.getNormalFieldsSelection());
			buffer.append(" FROM ");
			buffer.append(this.tempTableName);
			this.helper.buildWhereClause(buffer, deleteOp.getDelete().getWhere(), true, false);
			//buffer.append(")");
			String query = buffer.toString();

			int rowsDeleted = 0;
			ResultSet res = null;

			try
			{
				res = this.sqlInterface.executeQuery(query);
				PrimaryKeyValue pkValue = null;

				while(res.next())
				{
					if(!res.isLast())
						RuntimeUtils.throwRunTimeException("ResultSet should contain exactly 1 row",
								ExitCode.FETCH_RESULTS_ERROR);

					rowsDeleted++;
					pkValue = DatabaseCommon.getPrimaryKeyValue(res, this.databaseTable);

					this.recordedPkValues.remove(pkValue.getValue());
					this.duplicatedRows.remove(pkValue);
				}

				buffer = new StringBuilder();
				buffer.append("delete from ");
				buffer.append(this.tempTableName);
				buffer.append(" where ");
				buffer.append(deleteOp.getDelete().getWhere().toString());
				String delete = buffer.toString();

				this.sqlInterface.executeUpdate(delete);

				crdtOperation.setTableName(this.databaseTable.getName());
				crdtOperation.setUniquePkValue(pkValue.getUniqueValue());
				crdtOperation.setPrimaryKey(pkValue.getValue());
				//transaction.addToOpsList(crdtOperation);

				if(this.databaseTable.isParentTable())
				{
					crdtOperation.setOpType(CRDTOperationType.DELETE_PARENT);
					//TODO: delete parent cascade missing implementation
				} else
					crdtOperation.setOpType(CRDTOperationType.DELETE);

				return rowsDeleted;
			} finally
			{
				DbUtils.closeQuietly(res);
			}
		}

	}

	private int executeTempOpUpdate(SQLUpdate updateOp) throws SQLException
	{
		long loadingFromMain;
		long execUpdate;

		// before writting in the scratchpad, add the missing rows to the scratchpad
		long start = System.nanoTime();
		this.helper.addMissingRowsToScratchpad(updateOp);
		loadingFromMain = System.nanoTime() - start;
		this.txnRecord.addLoadfromMainTime(loadingFromMain);

		start = System.nanoTime();
		updateOp.prepareOperation(false, this.tempTableName);
		int result = this.sqlInterface.executeUpdate(updateOp.getSQLString());
		execUpdate = System.nanoTime() - start;
		this.txnRecord.addUpdateTime(execUpdate);

		return result;
	}

	private class ExecutionHelper
	{

		public static final String WHERE = " WHERE (";
		public static final String AND = " AND (";
		public static final String NOT_DELETED_EXPRESSION = DatabaseDefaults.DELETED_COLUMN + "=" + DatabaseDefaults
				.NOT_DELETED_VALUE;

		private String encloseWhereClauseWithBrackets(String clause)
		{
			return "(" + clause + ")";
		}

		protected void buildWhereClause(StringBuilder buffer, Expression e, boolean closeClause, boolean isTempTable)
		{
			if(e == null)
			{
				if(tablePolicy.allowDeletes() && isTempTable)
				{
					buffer.append(WHERE);
					buffer.append(encloseWhereClauseWithBrackets(NOT_DELETED_EXPRESSION));
					if(closeClause)
						buffer.append(")");
				}
			} else
			{
				buffer.append(WHERE);
				buffer.append(encloseWhereClauseWithBrackets(e.toString()));

				if(tablePolicy.allowDeletes() && isTempTable)
				{
					buffer.append(AND);
					buffer.append(encloseWhereClauseWithBrackets(NOT_DELETED_EXPRESSION));
				}

				if(closeClause)
					buffer.append(")");
			}
		}

		/**
		 * Inserts missing rows in the temporary table and returns the list of rows
		 * This must be done before updating rows in the scratchpad.
		 *
		 * @param updateOp
		 * @param pad
		 *
		 * @throws java.sql.SQLException
		 */
		private void addMissingRowsToScratchpad(SQLUpdate updateOp) throws SQLException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("SELECT ");
			buffer.append(databaseTable.getNormalFieldsSelection());
			buffer.append(", '" + updateOp.getUpdate().getTables().get(0).toString() + "' as tname FROM ");
			buffer.append(updateOp.getUpdate().getTables().get(0).toString());
			buildWhereClause(buffer, updateOp.getUpdate().getWhere(), true, true);
			//buffer.append(" AND ");
			//buffer.append(DatabaseDefaults.DELETED_COLUMN);
			buffer.append(" UNION SELECT ");
			buffer.append(databaseTable.getNormalFieldsSelection());
			buffer.append(", '" + tempTableName + "' as tname FROM ");
			buffer.append(tempTableName);
			buildWhereClause(buffer, updateOp.getUpdate().getWhere(), true, false);

			ResultSet res = null;
			try
			{
				res = sqlInterface.executeQuery(buffer.toString());

				while(res.next())
				{
					if(!res.getString("tname").equals(tempTableName))
					{
						if(!res.next())
						{
							//Debug.println("record exists in real table but not temp table");
							//affectedRows.add(res.getInt(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE));
							res.previous();
						} else
						{
							if(!res.getString("tname").equals(tempTableName))
							{
								//Debug.println("record exists in real table but not temp table");
								res.previous();
							} else
							{
								//Debug.println("record exists in both real and temp table");
								continue;
							}
						}
					} else
					{
						//Debug.println("record exist in temporary table but not real table");
						continue;
					}

					buffer.setLength(0);
					buffer.append("insert into ");
					buffer.append(tempTableName);
					buffer.append(" (");
					StringBuilder valuesBuffer = new StringBuilder(" VALUES (");

					PrimaryKeyValue pkValue = new PrimaryKeyValue(databaseTable.getName());

					Iterator<DataField> fieldsIt = fields.values().iterator();
					Record cachedRecord = updateOp.getCachedRecord();

					while(fieldsIt.hasNext())
					{
						DataField field = fieldsIt.next();

						buffer.append(field.getFieldName());
						if(fieldsIt.hasNext())
							buffer.append(",");

						String oldContent = res.getString(field.getFieldName());

						if(oldContent == null)
							if(field.isStringField())
								oldContent = "NULL";
							else if(field.isDateField())
								oldContent = IExecutorAgent.Defaults.DEFAULT_DATE_VALUE;

						if(field.isStringField() || field.isDateField())
						{
							valuesBuffer.append("'");
							valuesBuffer.append(oldContent);
							valuesBuffer.append("'");
						} else
							valuesBuffer.append(oldContent);

						if(fieldsIt.hasNext())
							valuesBuffer.append(",");

						if(!updateOp.isPrimaryKeySet())
						{
							if(field.isPrimaryKey())
							{
								FieldValue fValue = new FieldValue(field, oldContent);
								pkValue.addFieldValue(fValue);
							}
						}

						cachedRecord.addData(field.getFieldName(), oldContent);
					}

					if(!updateOp.isPrimaryKeySet())
					{
						pkValue.preparePrimaryKey();
						updateOp.setPrimaryKey(pkValue);
					}
					cachedRecords.put(pkValue, cachedRecord);

					buffer.append(")");
					buffer.append(valuesBuffer.toString());
					buffer.append(")");
					sqlInterface.executeUpdate(buffer.toString());
				}
			} finally
			{
				DbUtils.closeQuietly(res);
			}
		}

		private void generateNotInDeletedAndUpdatedClause(StringBuilder buffer)
		{
			if(duplicatedRows.size() == 0 && deletedRows.size() == 0)
				return;

			buffer.append("( ");
			// remove deleted and updated from select in main table
			InExpression notInExpression = new InExpression();
			notInExpression.setNot(true);

			List<Expression> deletedItemsList = new ArrayList<>();

			for(PrimaryKeyValue pkValue : duplicatedRows)
			{
				Expression valueExpression = new MyValueExpression("(" + pkValue.getValue() + ")");
				deletedItemsList.add(valueExpression);
			}

			for(PrimaryKeyValue pkValue : deletedRows)
			{
				Expression valueExpression = new MyValueExpression("(" + pkValue.getValue() + ")");
				deletedItemsList.add(valueExpression);
			}

			ExpressionList expressionList = new ExpressionList(deletedItemsList);

			notInExpression.setRightItemsList(expressionList);
			notInExpression.setLeftExpression(new MyValueExpression("(" + pk.getQueryClause() + ")"));

			buffer.append(notInExpression.toString());
			buffer.append(" )");
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

		private void verifyParentsConsistency(CRDTOperation crdtOperation, Row row, boolean isInsert)
				throws SQLException
		{
			Map<String, String> parentsByConstraint = null;

			if(isInsert)
			{
				if(fkConstraints.size() > 0)
				{
					crdtOperation.setOpType(CRDTOperationType.INSERT_CHILD);
					parentsByConstraint = findParentRows(row, fkConstraints, sqlInterface);
				} else
					crdtOperation.setOpType(CRDTOperationType.INSERT);
			} else
			{
				if(fkConstraints.size() > 0)
				{
					crdtOperation.setOpType(CRDTOperationType.UPDATE_CHILD);
					parentsByConstraint = findParentRows(row, fkConstraints, sqlInterface);
				} else
					crdtOperation.setOpType(CRDTOperationType.UPDATE);
			}

			if(parentsByConstraint != null)
				crdtOperation.setParentsMap(parentsByConstraint);

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

		private Record loadRecordFromResultSet(ResultSet rs) throws SQLException
		{
			Record record = new Record(databaseTable);
			PrimaryKeyValue pkValue = new PrimaryKeyValue(databaseTable.getName());

			for(DataField field : fields.values())
			{
				String value = rs.getString(field.getFieldName());

				if(value == null)
					value = "NULL";

				if(field.isPrimaryKey())
				{
					FieldValue fValue = new FieldValue(field, value);
					pkValue.addFieldValue(fValue);
				}

				record.addData(field.getFieldName(), value);

			}
			pkValue.preparePrimaryKey();
			return record;
		}
	}


	private class MyValueExpression implements Expression
	{

		private String value;

		public MyValueExpression(String value)
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return value;
		}

		@Override
		public void accept(ExpressionVisitor expressionVisitor)
		{
		}
	}
}
