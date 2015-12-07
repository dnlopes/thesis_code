package client.execution.temporary.scratchpad.agent;


import client.execution.TransactionRecord;
import client.execution.operation.*;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import common.database.constraints.fk.ForeignKeyConstraint;
import common.database.constraints.unique.AutoIncrementConstraint;
import common.database.constraints.unique.UniqueConstraint;
import common.database.SQLInterface;
import common.database.util.*;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.value.DeltaFieldValue;
import common.database.value.FieldValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import common.util.RuntimeUtils;
import applications.util.SymbolsManager;
import client.execution.QueryCreator;
import common.util.ExitCode;
import common.util.defaults.DatabaseDefaults;
import common.thrift.*;

import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 10/09/15.
 */
public class DBExecutorAgent extends AbstractExecAgent implements IExecutorAgent
{

	private static final int TEMPORARY_INTEGER = 10000;

	private ExecutionHelper helper;
	private Set<PrimaryKeyValue> duplicatedRows;
	private Set<PrimaryKeyValue> deletedRows;
	private Map<String, PrimaryKeyValue> recordedPkValues;

	public DBExecutorAgent(int sandboxId, int tableId, String tableName, SQLInterface sqlInterface,
						   ReadWriteScratchpad pad, TransactionRecord txnRecord) throws
			SQLException
	{
		super(sandboxId, tableId, tableName, sqlInterface, pad, txnRecord);

		this.duplicatedRows = new HashSet<>();
		this.deletedRows = new HashSet<>();
		this.recordedPkValues = new HashMap<>();
		this.helper = new ExecutionHelper();
	}

	@Override
	public ResultSet executeTemporaryQuery(Select selectOp) throws SQLException
	{
		String queryToOrigin;
		String queryToTemp;

		StringBuilder buffer = new StringBuilder();

		PlainSelect plainSelect = (PlainSelect) selectOp.getSelectBody();

		if(plainSelect.isForUpdate())
			plainSelect.setForUpdate(false);

		List columnsToFetch = plainSelect.getSelectItems();

		if(columnsToFetch.size() == 1 && columnsToFetch.get(0).toString().equalsIgnoreCase("*"))
			plainSelect.setSelectItems(this.selectAllItems);

		StringBuilder whereClauseTemp = new StringBuilder();

		if(plainSelect.getWhere() != null)
			whereClauseTemp.append(plainSelect.getWhere());

		StringBuilder whereClauseOrig = new StringBuilder(whereClauseTemp);

		if(this.tablePolicy.allowDeletes())
		{
			whereClauseOrig.append(" AND ");
			whereClauseOrig.append(ExecutionHelper.NOT_DELETED_EXPRESSION);
		}

		if(this.duplicatedRows.size() > 0 || this.deletedRows.size() > 0)
		{
			whereClauseOrig.append(" AND ");
			this.helper.generateNotInDeletedAndUpdatedClause(whereClauseOrig);
		}

		queryToOrigin = plainSelect.toString();

		plainSelect.setFromItem(this.fromItemTemp);
		queryToTemp = plainSelect.toString();

		if(plainSelect.getWhere() != null)
		{
			String defaultWhere = plainSelect.getWhere().toString();
			queryToOrigin = StringUtils.replace(queryToOrigin, defaultWhere, whereClauseOrig.toString());
			queryToTemp = StringUtils.replace(queryToTemp, defaultWhere, whereClauseTemp.toString());
		} else
		{
			StringBuilder auxBuffer = new StringBuilder(queryToOrigin);
			auxBuffer.append(" WHERE ");
			auxBuffer.append(whereClauseOrig);
			queryToOrigin = auxBuffer.toString();
			auxBuffer.setLength(0);
			auxBuffer.append(queryToTemp);
			auxBuffer.append(" WHERE ");
			auxBuffer.append(whereClauseTemp);
			queryToTemp = auxBuffer.toString();
		}

		buffer.append("(");
		buffer.append(queryToTemp);
		buffer.append(")");
		buffer.append(" UNION ");
		buffer.append("(");
		buffer.append(queryToOrigin);
		buffer.append(")");

		String finalQuery = buffer.toString();

		return this.sqlInterface.executeQuery(finalQuery);
	}

	@Override
	public int executeTemporaryUpdate(SQLWriteOperation sqlOp)
			throws SQLException
	{
		//TODO currently not usable
		if(sqlOp.getOpType() == SQLOperationType.DELETE)
		{
			return executeTempOpDelete((SQLDelete) sqlOp, null);
		} else
		{
			if(sqlOp.getOpType() == SQLOperationType.INSERT)
			{
				return executeTempOpInsert((SQLInsert) sqlOp, null);
			} else if(sqlOp.getOpType() == SQLOperationType.UPDATE)
			{
				return executeTempOpUpdate((SQLUpdate) sqlOp, null);
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
	}

	public void prepareForCommit() throws SQLException
	{
		this.sqlInterface.executeQuery("SELECT * FROM " + this.tempTableName);
	}

	private int executeTempOpInsert(SQLInsert insertOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder("(");
		buffer.append("insert into ");
		buffer.append(tempTableName);

		List columnsList = insertOp.getInsert().getColumns();
		List valuesList = ((ExpressionList) insertOp.getInsert().getItemsList()).getExpressions();
		PrimaryKeyValue pkValue = new PrimaryKeyValue(this.databaseTable.getName());
		Row insertedRow = new Row(this.databaseTable, pkValue);

		Set<UniqueConstraint> toCheckConstraint = new HashSet<>();

		Map<String, String> fieldsMap = new HashMap<>();

		if(columnsList == null)
		{
			buffer.append(" (");
			buffer.append(tableDefinition.getPlainColumnList());
			buffer.append(")");
		} else
		{
			buffer.append(" (");

			Iterator colIt = columnsList.iterator();
			Iterator valIt = valuesList.iterator();
			boolean first = true;

			while(colIt.hasNext())
			{
				String col = colIt.next().toString();
				String val = valIt.next().toString();
				DataField field = this.fields.get(col);

				if(val.contains(SymbolsManager.SYMBOL_PREFIX))
				{
					this.helper.createSymbolEntry(transaction, crdtOperation, val, field);

					if(field.isNumberField())
						val = String.valueOf(TEMPORARY_INTEGER);
				}

				if(!field.isHiddenField())
				{
					FieldValue newContentField = new FieldValue(field, val);
					insertedRow.updateFieldValue(newContentField);

					fieldsMap.put(col, val);

					if(field.isPrimaryKey())
						pkValue.addFieldValue(newContentField);
				}

				if(!first)
				{
					buffer.append(",");
					valuesBuffer.append(",");
				}

				first = false;
				buffer.append(col);
				valuesBuffer.append(val);

				for(UniqueConstraint c : field.getUniqueConstraints())
					if(c.requiresCoordination())
						toCheckConstraint.add(c);

			}

			buffer.append(")");
			valuesBuffer.append(")");
		}

		buffer.append(" values ");
		buffer.append(valuesBuffer);

		int result = this.sqlInterface.executeUpdate(buffer.toString());

		crdtOperation.setTableName(this.databaseTable.getName());
		crdtOperation.setUniquePkValue(pkValue.getUniqueValue());
		crdtOperation.setPrimaryKey(pkValue.getValue());
		crdtOperation.setNewFieldValues(fieldsMap);
		crdtOperation.setPkWhereClause(insertedRow.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		this.helper.verifyParentsConsistency(crdtOperation, insertedRow, true);

		transaction.addToOpsList(crdtOperation);

		for(UniqueConstraint uniqueConstraint : toCheckConstraint)
		{
			List<DataField> fieldsList = uniqueConstraint.getFields();
			StringBuilder uniqueBuffer = new StringBuilder();

			for(DataField aField : fieldsList)
			{
				uniqueBuffer.append(insertedRow.getFieldValue(aField.getFieldName()));
				uniqueBuffer.append("_");
			}

			uniqueBuffer.setLength(uniqueBuffer.length() - 1);
			UniqueValue uniqueRequest = new UniqueValue();
			uniqueRequest.setConstraintId(uniqueConstraint.getConstraintIdentifier());
			uniqueRequest.setValue(uniqueBuffer.toString());

			if(!transaction.isSetRequestToCoordinator())
				transaction.setRequestToCoordinator(new CoordinatorRequest());

			CoordinatorRequest coordinatorRequest = transaction.getRequestToCoordinator();

			if(!coordinatorRequest.isSetUniqueValues())
				coordinatorRequest.setUniqueValues(new LinkedList<UniqueValue>());

			coordinatorRequest.addToUniqueValues(uniqueRequest);
		}

		return result;
	}

	private int executeTempOpDelete(SQLDelete deleteOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(this.databaseTable.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(deleteOp.getDelete().getTable().toString());
		this.helper.buildWhereClause(buffer, deleteOp.getDelete().getWhere(), true);

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
		this.helper.buildWhereClause(buffer, deleteOp.getDelete().getWhere(), true);
		buffer.append(")");
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
			transaction.addToOpsList(crdtOperation);

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

	private int executeTempOpUpdate(SQLUpdate updateOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		// before writting in the scratchpad, add the missing rows to the scratchpad
		this.helper.addMissingRowsToScratchpad(updateOp.getUpdate());
		Row updatedRow = this.helper.getUpdatedRowFromDatabase(updateOp.getUpdate());

		Map<String, String> oldValuesMap = new HashMap<>();
		Map<String, String> newValuesMap = new HashMap<>();
		Set<UniqueConstraint> toCheckConstraint = new HashSet<>();
		Map<String, FieldValue> currentRowValues = updatedRow.getFieldsValuesMap();

		for(Map.Entry<String, FieldValue> entry : currentRowValues.entrySet())
			oldValuesMap.put(entry.getKey(), entry.getValue().getFormattedValue());

		// now perform the actual update only in the scratchpad
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(this.tempTableName);
		buffer.append(" SET ");
		Iterator colIt = updateOp.getUpdate().getColumns().iterator();
		Iterator expIt = updateOp.getUpdate().getExpressions().iterator();

		while(colIt.hasNext())
		{
			String columnName = colIt.next().toString();
			String newValue = expIt.next().toString();
			DataField field = this.fields.get(columnName);

			if(field.isHiddenField())
				continue;

			/*
			for(CheckConstraint checkConstraint : field.getCheckConstraints())
			{
				//TODO verify if value is valid under these check constraints
			}
			*/

			// we do not allow updates on primary keys, foreign keys and immutable fields
			if(field.isImmutableField() || field.isPrimaryKey() || field.hasChilds())
				RuntimeUtils.throwRunTimeException(
						"trying to modify a primary key, a foreign key or an " + "immutable field",
						ExitCode.UNEXPECTED_OP);

			if(newValue == null)
				newValue = "NULL";

			FieldValue newFieldValue;

			if(field.isDeltaField())
				newFieldValue = new DeltaFieldValue(field, newValue,
						updatedRow.getFieldValue(field.getFieldName()).getValue());
			else
				newFieldValue = new FieldValue(field, newValue);

			updatedRow.updateFieldValue(newFieldValue);
			newValuesMap.put(columnName, newFieldValue.getFormattedValue());

			for(UniqueConstraint c : field.getUniqueConstraints())
				if(c.requiresCoordination())
					toCheckConstraint.add(c);

			buffer.append(columnName);
			buffer.append("=");
			buffer.append(newFieldValue.getFormattedValue());
			if(colIt.hasNext())
				buffer.append(",");
		}

		buffer.append(" WHERE ");
		buffer.append(updateOp.getUpdate().getWhere().toString());
		String updateStr = buffer.toString();
		this.sqlInterface.executeUpdate(updateStr);

		int affectedRows = 1;

		crdtOperation.setTableName(this.databaseTable.getName());
		crdtOperation.setUniquePkValue(updatedRow.getPrimaryKeyValue().getUniqueValue());
		crdtOperation.setPrimaryKey(updatedRow.getPrimaryKeyValue().getValue());
		crdtOperation.setNewFieldValues(newValuesMap);
		crdtOperation.setOldFieldValues(oldValuesMap);
		crdtOperation.setPkWhereClause(updatedRow.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		transaction.addToOpsList(crdtOperation);

		for(UniqueConstraint uniqueConstraint : toCheckConstraint)
		{
			List<DataField> fieldsList = uniqueConstraint.getFields();
			StringBuilder uniqueBuffer = new StringBuilder();

			for(DataField aField : fieldsList)
			{
				uniqueBuffer.append(updatedRow.getFieldValue(aField.getFieldName()));
				uniqueBuffer.append("_");
			}

			uniqueBuffer.setLength(uniqueBuffer.length() - 1);

			UniqueValue uniqueRequest = new UniqueValue();
			uniqueRequest.setConstraintId(uniqueConstraint.getConstraintIdentifier());
			uniqueRequest.setValue(uniqueBuffer.toString());

			if(!transaction.isSetRequestToCoordinator())
				transaction.setRequestToCoordinator(new CoordinatorRequest());

			CoordinatorRequest coordinatorRequest = transaction.getRequestToCoordinator();

			if(!coordinatorRequest.isSetUniqueValues())
				coordinatorRequest.setUniqueValues(new LinkedList<UniqueValue>());

			coordinatorRequest.addToUniqueValues(uniqueRequest);
		}

		this.helper.verifyParentsConsistency(crdtOperation, updatedRow, false);

		return affectedRows;
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

		protected void buildWhereClause(StringBuilder buffer, Expression e, boolean closeClause)
		{
			if(e == null)
			{
				if(tablePolicy.allowDeletes())
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

				if(tablePolicy.allowDeletes())
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
		private void addMissingRowsToScratchpad(Update updateOp) throws SQLException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("SELECT ");
			buffer.append(databaseTable.getNormalFieldsSelection());
			buffer.append(", '" + updateOp.getTables().get(0).toString() + "' as tname FROM ");
			buffer.append(updateOp.getTables().get(0).toString());
			buildWhereClause(buffer, updateOp.getWhere(), true);
			//buffer.append(" AND ");
			//buffer.append(DatabaseDefaults.DELETED_COLUMN);
			buffer.append(" UNION SELECT ");
			buffer.append(databaseTable.getNormalFieldsSelection());
			buffer.append(", '" + tempTableName + "' as tname FROM ");
			buffer.append(tempTableName);
			buildWhereClause(buffer, updateOp.getWhere(), true);

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
					//buffer.append(" values (");

					PrimaryKeyValue pkValue = new PrimaryKeyValue(databaseTable.getName());

					Iterator<DataField> fieldsIt = fields.values().iterator();

					while(fieldsIt.hasNext())
					{
						DataField field = fieldsIt.next();

						buffer.append(field.getFieldName());
						if(fieldsIt.hasNext())
							buffer.append(",");

						String oldContent = res.getString(field.getFieldName());

						if(oldContent == null)
							oldContent = "NULL";

					/*	if(oldContent == null)
							if(field.isStringField())
								oldContent = "NULL";
							else if(field.isDateField())
								oldContent = IExecutorAgent.Defaults.DEFAULT_DATE_VALUE;
                       */
						if(field.isStringField() || field.isDateField())
						{
							valuesBuffer.append("'");
							valuesBuffer.append(oldContent);
							valuesBuffer.append("'");
						} else
							valuesBuffer.append(oldContent);

						if(fieldsIt.hasNext())
							valuesBuffer.append(",");

						if(field.isPrimaryKey())
						{
							FieldValue fValue = new FieldValue(field, oldContent);
							pkValue.addFieldValue(fValue);
						}
					}

					buffer.append(")");
					buffer.append(valuesBuffer.toString());
					//duplicatedRows.add(pkValue);
					buffer.append(")");
					sqlInterface.executeUpdate(buffer.toString());
				}
			} finally
			{
				DbUtils.closeQuietly(res);
			}
		}

		private Row getUpdatedRowFromDatabase(Update updateOp) throws SQLException
		{
			Expression whereClause = updateOp.getWhere();
			if(whereClause == null)
				RuntimeUtils.throwRunTimeException(
						"update operation should specify a primary key in the where " + "clause",
						ExitCode.INVALIDUSAGE);

			StringBuilder buffer = new StringBuilder();
			buffer.append("SELECT ");
			buffer.append(databaseTable.getNormalFieldsSelection());
			buffer.append(" FROM ");
			buffer.append(tempTableName);
			buffer.append(" WHERE ");
			buffer.append(whereClause);

			ResultSet rs = null;
			try
			{
				rs = sqlInterface.executeQuery(buffer.toString());

				if(!rs.isBeforeFirst())
				{
					if(LOG.isDebugEnabled())
						LOG.debug(buffer.toString());
					throw new SQLException("result set is empty (could not fetch row from main storage)");
				}

				rs.next();

				Row row = DatabaseCommon.getFullRow(rs, databaseTable);
				if(row != null)
					return row;

				RuntimeUtils.throwRunTimeException("error fetching updated row from database",
						ExitCode.FETCH_RESULTS_ERROR);

				return null;

			} finally
			{
				DbUtils.closeQuietly(rs);
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

		private void createSymbolEntry(CRDTTransaction txn, CRDTOperation op, String symbol, DataField dataField)
		{
			if(!txn.isSetSymbolsMap())
				txn.setSymbolsMap(new HashMap<String, SymbolEntry>());

			Map<String, SymbolEntry> symbolsMap = txn.getSymbolsMap();

			if(symbolsMap.containsKey(symbol))
			{
				linkSymbolToField(op, symbolsMap.get(symbol), dataField);
				return;
			} else //create new symbol entry
			{
				SymbolEntry symbolEntry = new SymbolEntry();
				symbolEntry.setSymbol(symbol);
				symbolEntry.setTableName(dataField.getTableName());
				symbolEntry.setFieldName(dataField.getFieldName());
				symbolEntry.setRequiresCoordination(false);
				symbolsMap.put(symbol, symbolEntry);
				linkSymbolToField(op, symbolEntry, dataField);
			}

			if(!dataField.isAutoIncrement())
				RuntimeUtils.throwRunTimeException(
						"field with semantic value must either be auto_increment or " + "given by the " +
								"application" +
								" " +
								"level", ExitCode.INVALIDUSAGE);

			if(dataField.getSemantic() == SemanticPolicy.SEMANTIC)
			{
				SymbolEntry symbolEntry = symbolsMap.get(symbol);
				symbolEntry.setRequiresCoordination(true);

				AutoIncrementConstraint autoIncrementConstraint = databaseTable.getAutoIncrementConstraint(
						dataField.getFieldName());
				RequestValue request = new RequestValue();

				request.setOpId(op.getOpId());
				request.setConstraintId(autoIncrementConstraint.getConstraintIdentifier());
				request.setTempSymbol(symbol);
				request.setFieldName(dataField.getFieldName());

				if(!txn.isSetRequestToCoordinator())
					txn.setRequestToCoordinator(new CoordinatorRequest());

				txn.getRequestToCoordinator().addToRequests(request);
			}
		}

		private void linkSymbolToField(CRDTOperation op, SymbolEntry symbolEntry, DataField dataField)
		{
			if(!op.isSetSymbolFieldMap())
				op.setSymbolFieldMap(new HashMap<String, String>());

			Map<String, String> symbolFieldMap = op.getSymbolFieldMap();
			symbolFieldMap.put(symbolEntry.getSymbol(), dataField.getFieldName());
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