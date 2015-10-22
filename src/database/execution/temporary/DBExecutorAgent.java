package database.execution.temporary;


import database.constraints.Constraint;
import database.constraints.ConstraintType;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.unique.AutoIncrementConstraint;
import database.execution.SQLInterface;
import database.util.*;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import database.util.value.DeltaFieldValue;
import database.util.value.FieldValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import runtime.SymbolsManager;
import runtime.transformer.QueryCreator;
import util.ExitCode;
import util.Configuration;
import util.defaults.DatabaseDefaults;
import util.defaults.ScratchpadDefaults;
import util.thrift.*;

import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 10/09/15.
 */
public class DBExecutorAgent implements IExecutorAgent
{

	private static final String SP_DELETED_EXPRESSION = DatabaseDefaults.DELETED_COLUMN + "=0";
	private static final Logger LOG = LoggerFactory.getLogger(DBExecutorAgent.class);
	private static final int TEMPORARY_INTEGER = 10000;

	private SQLInterface sqlInterface;

	private TableDefinition tableDefinition;
	private DatabaseTable databaseTable;
	private List<ForeignKeyConstraint> fkConstraints;
	private Map<String, DataField> fields;
	private String tempTableName;
	private String tempTableNameAlias;
	private int tableId;
	private int sandboxId;

	private FromItem fromItemTemp;
	private PrimaryKey pk;
	private List<SelectItem> selectAllItems;

	private Set<PrimaryKeyValue> duplicatedRows;
	private Set<PrimaryKeyValue> deletedRows;
	private Map<String, PrimaryKeyValue> recordedPkValues;

	public DBExecutorAgent(int sandboxId, int tableId, String tableName, SQLInterface sqlInterface) throws SQLException
	{
		this.sandboxId = sandboxId;
		this.tableId = tableId;
		this.fkConstraints = new ArrayList<>();

		this.databaseTable = Configuration.getInstance().getDatabaseMetadata().getTable(tableName);
		for(Constraint c : this.databaseTable.getTableInvarists())
		{
			if(c instanceof ForeignKeyConstraint)
				this.fkConstraints.add((ForeignKeyConstraint) c);
		}

		this.pk = databaseTable.getPrimaryKey();
		this.selectAllItems = new ArrayList<>();
		this.fields = this.databaseTable.getFieldsMap();

		for(DataField field : this.fields.values())
		{
			if(field.isHiddenField())
				continue;

			Column column = new Column(field.getFieldName());
			SelectExpressionItem a = new SelectExpressionItem(column);
			selectAllItems.add(a);
		}

		this.duplicatedRows = new HashSet<>();
		this.deletedRows = new HashSet<>();
		this.recordedPkValues = new HashMap<>();

		this.sqlInterface = sqlInterface;
	}

	@Override
	public void setup(DatabaseMetaData metadata, int scratchpadId)
	{
		try
		{
			this.tempTableName = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.databaseTable.getName() +
					"_" +
					scratchpadId;
			this.fromItemTemp = new Table(Configuration.getInstance().getDatabaseName(), tempTableName);
			this.tempTableNameAlias = ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + this.tableId;
			String tableNameAlias = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.tableId;

			StringBuilder buffer2 = new StringBuilder();
			buffer2.append("DROP TABLE IF EXISTS ");
			StringBuilder buffer = new StringBuilder();

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append("CREATE LOCAL TEMPORARY TABLE ");    // for H2
			else
				buffer.append("CREATE TABLE IF NOT EXISTS ");        // for mysql

			if(LOG.isTraceEnabled())
				LOG.trace("creating temporary table {}", this.tempTableName);

			buffer.append(tempTableName);
			buffer2.append(tempTableName);
			buffer2.append(";");
			buffer.append("(");

			ArrayList<Boolean> tempIsStr = new ArrayList<>();        // for columns
			ArrayList<String> temp = new ArrayList<>();        // for columns
			ArrayList<String> tempAlias = new ArrayList<>();    // for columns with aliases
			ArrayList<String> tempTempAlias = new ArrayList<>();    // for temp columns with aliases
			ArrayList<String> uniqueIndices = new ArrayList<>(); // unique index
			ResultSet colSet = metadata.getColumns(null, null, this.databaseTable.getName(), "%");
			boolean first = true;

			while(colSet.next())
			{
				if(!first)
					buffer.append(",");
				else
					first = false;
				buffer.append(colSet.getString(4));            // column name
				buffer.append(" ");
				String[] tmpStr = {""};
				if(colSet.getString(6).contains(" "))
				{        // column type
					tmpStr = colSet.getString(6).split(" ");
				} else
				{
					tmpStr[0] = colSet.getString(6);
				}

				buffer.append(tmpStr[0]);
				if(!(tmpStr[0].equals("INT") ||
						tmpStr[0].equals("DOUBLE") ||
						tmpStr[0].equals("BIT") ||
						tmpStr[0].equals("DATE") ||
						tmpStr[0].equals("TIME") ||
						tmpStr[0].equals("TIMESTAMP") ||
						tmpStr[0].equals("DATETIME") ||
						tmpStr[0].equals("YEAR")))
				{
					buffer.append("(");
					buffer.append(colSet.getInt(7));        //size of type
					buffer.append(")");
				}
				buffer.append(" ");
				if(tmpStr.length > 1)
					buffer.append(tmpStr[1]);
				if(colSet.getString(4).equalsIgnoreCase(DatabaseDefaults.DELETED_CLOCK_COLUMN))
				{
					buffer.append(" DEFAULT FALSE ");
				}

				temp.add(colSet.getString(4));
				tempAlias.add(tableNameAlias + "." + colSet.getString(4));
				tempTempAlias.add(this.tempTableNameAlias + "." + colSet.getString(4));
				tempIsStr.add(
						colSet.getInt(5) == Types.VARCHAR || colSet.getInt(5) == Types.LONGNVARCHAR || colSet.getInt(
								5) == Types.LONGVARCHAR || colSet.getInt(5) == Types.CHAR || colSet.getInt(
								5) == Types.DATE || colSet.getInt(5) == Types.TIMESTAMP || colSet.getInt(
								5) == Types.TIME);
			}
			colSet.close();
			String[] cols = new String[temp.size()];
			temp.toArray(cols);
			temp.clear();

			String[] aliasCols = new String[tempAlias.size()];
			tempAlias.toArray(aliasCols);
			tempAlias.clear();

			String[] tempAliasCols = new String[tempTempAlias.size()];
			tempTempAlias.toArray(tempAliasCols);
			tempTempAlias.clear();

			boolean[] colsIsStr = new boolean[tempIsStr.size()];
			for(int i = 0; i < colsIsStr.length; i++)
				colsIsStr[i] = tempIsStr.get(i);

			//get all unique index
			ResultSet uqIndices = metadata.getIndexInfo(null, null, this.databaseTable.getName(), true, true);
			while(uqIndices.next())
			{
				String indexName = uqIndices.getString("INDEX_NAME");
				String columnName = uqIndices.getString("COLUMN_NAME");
				if(indexName == null)
				{
					continue;
				}
				uniqueIndices.add(columnName);
			}
			uqIndices.close();

			ResultSet pkSet = metadata.getPrimaryKeys(null, null, this.databaseTable.getName());
			while(pkSet.next())
			{
				if(temp.size() == 0)
					buffer.append(", PRIMARY KEY (");
				else
					buffer.append(", ");
				buffer.append(pkSet.getString(4));
				temp.add(pkSet.getString(4));
				tempAlias.add(tableNameAlias + "." + pkSet.getString(4));
				tempTempAlias.add(this.tempTableNameAlias + "." + pkSet.getString(4));
				uniqueIndices.remove(pkSet.getString(4));
			}
			pkSet.close();
			if(temp.size() > 0)
				buffer.append(")");
			String[] pkPlain = new String[temp.size()];
			temp.toArray(pkPlain);
			temp.clear();

			String[] pkAlias = new String[tempAlias.size()];
			tempAlias.toArray(pkAlias);
			tempAlias.clear();

			String[] pkTempAlias = new String[tempTempAlias.size()];
			tempTempAlias.toArray(pkTempAlias);
			tempTempAlias.clear();

			String[] uqIndicesPlain = new String[uniqueIndices.size()];
			uniqueIndices.toArray(uqIndicesPlain);
			uniqueIndices.clear();

			this.tableDefinition = new TableDefinition(this.databaseTable.getName(), tableNameAlias, this.tableId,
					colsIsStr, cols, aliasCols, tempAliasCols, pkPlain, pkAlias, pkTempAlias, uqIndicesPlain);

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append(") NOT PERSISTENT;");    // FOR H2
			else
				buffer.append(") ENGINE=MEMORY;");    // FOR MYSQL

			this.sqlInterface.executeUpdate(buffer2.toString());
			this.sqlInterface.executeUpdate(buffer.toString());

		} catch(SQLException e)
		{
			LOG.error("failed to create temporary tables for scratchpad", e);
			RuntimeUtils.throwRunTimeException("scratchpad creation failed", ExitCode.SCRATCHPAD_INIT_FAILED);
		}
		if(LOG.isTraceEnabled())
			LOG.trace("executor for table {} created", this.databaseTable.getName());
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
		whereClauseOrig.append(" AND ");
		whereClauseOrig.append(SP_DELETED_EXPRESSION);

		if(this.duplicatedRows.size() > 0 || this.deletedRows.size() > 0)
		{
			whereClauseOrig.append(" AND ");
			this.generateNotInDeletedAndUpdatedClause(whereClauseOrig);
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
	public int executeTemporaryUpdate(net.sf.jsqlparser.statement.Statement statement, CRDTTransaction transaction)
			throws SQLException
	{
		if(statement instanceof Delete)
			return executeTempOpDelete((Delete) statement, transaction);

		else
		{
			if(statement instanceof Insert)
				return executeTempOpInsert((Insert) statement, transaction);
			else if(statement instanceof Update)
				return executeTempOpUpdate((Update) statement, transaction);
			else
				throw new SQLException("update statement not found");
		}
	}

	@Override
	public void resetExecuter() throws SQLException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("TRUNCATE TABLE ");
		buffer.append(this.tempTableName);
		this.sqlInterface.executeUpdate(buffer.toString());
		this.duplicatedRows.clear();
		this.recordedPkValues.clear();
		this.deletedRows.clear();
	}

	protected void addWhere(StringBuilder buffer, Expression e)
	{
		if(e == null)
			return;
		buffer.append(" WHERE (");
		buffer.append(SP_DELETED_EXPRESSION);
		buffer.append(")");
		if(e != null)
		{
			buffer.append(" AND ( ");
			buffer.append(e.toString());
			buffer.append(" ) ");
		}
	}

	private int executeTempOpInsert(Insert insertOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder("(");
		buffer.append("insert into ");
		buffer.append(tempTableName);

		List columnsList = insertOp.getColumns();
		List valuesList = ((ExpressionList) insertOp.getItemsList()).getExpressions();
		PrimaryKeyValue pkValue = new PrimaryKeyValue(this.databaseTable.getName());
		Row insertedRow = new Row(this.databaseTable, pkValue);

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
					createSymbolEntry(transaction, crdtOperation, val, field);
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

				for(Constraint c : field.getInvariants())
				{
					if(c.requiresCoordination() && c.getType() == ConstraintType.UNIQUE)
					{

					}
				}
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

		this.verifyParentsConsistency(crdtOperation, insertedRow, true);
		transaction.addToOpsList(crdtOperation);

		return result;
	}

	private int executeTempOpDelete(Delete deleteOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		StringBuilder buffer = new StringBuilder();
		buffer.append("(SELECT ");
		buffer.append(this.databaseTable.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(deleteOp.getTable().toString());
		addWhere(buffer, deleteOp.getWhere());
		if(this.duplicatedRows.size() > 0 || this.deletedRows.size() > 0)
		{
			buffer.append("AND ");
			this.generateNotInDeletedAndUpdatedClause(buffer);
		}
		buffer.append(") UNION (SELECT ");
		buffer.append(this.databaseTable.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, deleteOp.getWhere());
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
			buffer.append(deleteOp.getWhere().toString());
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
				//this.calculateOperationSideEffects((DeleteParentOperation) op, rowToDelete);
				//rowsDeleted += ((DeleteParentOperation) op).getNumberOfRows();

			} else
				crdtOperation.setOpType(CRDTOperationType.DELETE);

			return rowsDeleted;
		} finally
		{
			DbUtils.closeQuietly(res);
		}
	}

	private int executeTempOpUpdate(Update updateOp, CRDTTransaction transaction) throws SQLException
	{
		CRDTOperation crdtOperation = new CRDTOperation();
		crdtOperation.setOpId(transaction.getOpsListSize());

		// before writting in the scratchpad, add the missing rows to the scratchpad
		this.addMissingRowsToScratchpad(updateOp);
		Row updatedRow = this.getUpdatedRowFromDatabase(updateOp);

		Map<String, String> oldValuesMap = new HashMap<>();
		Map<String, String> newValuesMap = new HashMap<>();

		Map<String, FieldValue> currentRowValues = updatedRow.getFieldsValuesMap();

		for(Map.Entry<String, FieldValue> entry : currentRowValues.entrySet())
			oldValuesMap.put(entry.getKey(), entry.getValue().getFormattedValue());

		// now perform the actual update only in the scratchpad
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(this.tempTableName);
		buffer.append(" SET ");
		Iterator colIt = updateOp.getColumns().iterator();
		Iterator expIt = updateOp.getExpressions().iterator();

		while(colIt.hasNext())
		{
			String columnName = colIt.next().toString();
			String newValue = expIt.next().toString();
			DataField field = this.fields.get(columnName);

			if(field.isHiddenField())
				continue;

			// currently, we do not allow updates on primary keys, foreign keys and immutable fields
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

			for(Constraint c : field.getInvariants())
			{
				if(c.getType() == ConstraintType.CHECK || c.getType() == ConstraintType.UNIQUE || c.getType() ==
						ConstraintType.AUTO_INCREMENT)
					updatedRow.addConstraintToverify(c);
			}

			buffer.append(columnName);
			buffer.append("=");
			buffer.append(newFieldValue.getFormattedValue());
			if(colIt.hasNext())
				buffer.append(",");
		}

		buffer.append(" WHERE ");
		buffer.append(updateOp.getWhere().toString());
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

		this.verifyParentsConsistency(crdtOperation, updatedRow, false);

		return affectedRows;
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
		buffer.append("(SELECT *, '" + updateOp.getTables().get(0).toString() + "' as tname FROM ");
		buffer.append(updateOp.getTables().get(0).toString());
		addWhere(buffer, updateOp.getWhere());
		//buffer.append(") UNION (select *, '" + this.tempTableName + "' as tname FROM ");
		buffer.append(" AND ");
		buffer.append(DatabaseDefaults.DELETED_COLUMN);
		buffer.append("=0) UNION (select *, '" + this.tempTableName + "' as tname FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, updateOp.getWhere());
		//buffer.append(");");
		buffer.append(" AND ");
		buffer.append(DatabaseDefaults.DELETED_COLUMN);
		buffer.append("=0)");

		ResultSet res = null;
		try
		{
			res = this.sqlInterface.executeQuery(buffer.toString());

			while(res.next())
			{
				if(!res.getString("tname").equals(this.tempTableName))
				{
					if(!res.next())
					{
						//Debug.println("record exists in real table but not temp table");
						//affectedRows.add(res.getInt(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE));
						res.previous();
					} else
					{
						if(!res.getString("tname").equals(this.tempTableName))
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
				buffer.append(this.tempTableName);
				buffer.append(" values (");

				PrimaryKeyValue pkValue = new PrimaryKeyValue(this.databaseTable.getName());

				Iterator<DataField> fieldsIt = this.fields.values().iterator();

				while(fieldsIt.hasNext())
				{
					DataField field = fieldsIt.next();

					String oldContent;

					if(!field.isDeletedFlagField())
						oldContent = res.getString(field.getFieldName());
					else
					{
						oldContent = Integer.toString(res.getInt(field.getFieldName()));
						oldContent = String.valueOf(oldContent);
					}

					if(oldContent == null)
						if(field.isStringField())
							oldContent = "NULL";
						else if(field.isDateField())
							oldContent = IExecutorAgent.Defaults.DEFAULT_DATE_VALUE;

					if(field.isStringField() || field.isDateField())
					{
						buffer.append("'");
						buffer.append(oldContent);
						buffer.append("'");
					} else
						buffer.append(oldContent);

					if(fieldsIt.hasNext())
						buffer.append(",");

					if(field.isPrimaryKey())
					{
						FieldValue fValue = new FieldValue(field, oldContent);
						pkValue.addFieldValue(fValue);
					}
				}

				duplicatedRows.add(pkValue);
				buffer.append(")");
				this.sqlInterface.executeUpdate(buffer.toString());
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
			RuntimeUtils.throwRunTimeException("update operation should specify a primary key in the where " +
							"clause",
					ExitCode.INVALIDUSAGE);

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(this.databaseTable.getNormalFieldsSelection());
		buffer.append(" FROM ");
		buffer.append(this.tempTableName);
		buffer.append(" WHERE ");
		buffer.append(whereClause);

		ResultSet rs = null;
		try
		{
			rs = this.sqlInterface.executeQuery(buffer.toString());

			if(!rs.isBeforeFirst())
			{
				if(LOG.isDebugEnabled())
					LOG.debug(buffer.toString());
				throw new SQLException("result set is empty (could not fetch row from main storage)");
			}

			rs.next();

			Row row = DatabaseCommon.getFullRow(rs, this.databaseTable);
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
		if(this.duplicatedRows.size() == 0 && this.deletedRows.size() == 0)
			return;

		buffer.append("( ");
		// remove deleted and updated from select in main table
		InExpression notInExpression = new InExpression();
		notInExpression.setNot(true);

		List<Expression> deletedItemsList = new ArrayList<>();

		for(PrimaryKeyValue pkValue : this.duplicatedRows)
		{
			Expression valueExpression = new MyValueExpression("(" + pkValue.getValue() + ")");
			deletedItemsList.add(valueExpression);
		}

		for(PrimaryKeyValue pkValue : this.deletedRows)
		{
			Expression valueExpression = new MyValueExpression("(" + pkValue.getValue() + ")");
			deletedItemsList.add(valueExpression);
		}

		ExpressionList expressionList = new ExpressionList(deletedItemsList);

		notInExpression.setRightItemsList(expressionList);
		notInExpression.setLeftExpression(new MyValueExpression("(" + this.pk.getQueryClause() + ")"));

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

			if(!c.requiresParentConsistency())
				continue;

			Row parent = findParent(childRow, c, sqlInterface);
			parentByConstraint.put(c.getConstraintIdentifier(), parent.getPrimaryKeyValue().getPrimaryKeyWhereClause
					());

			if(parent == null)
				throw new SQLException("parent row not found. Foreing key violated");
		}

		//return null in the case where app never deletes any parent
		if(parentByConstraint.size() == 0)
			return null;
		else
			return parentByConstraint;
	}

	private void verifyParentsConsistency(CRDTOperation crdtOperation, Row row, boolean isInsert) throws SQLException
	{
		if(this.fkConstraints.size() > 0) // its a child row
		{
			if(isInsert)
				crdtOperation.setOpType(CRDTOperationType.INSERT_CHILD);
			else
				crdtOperation.setOpType(CRDTOperationType.UPDATE_CHILD);
			Map<String, String> parentsByConstraint = findParentRows(row, this.fkConstraints, this.sqlInterface);

			if(parentsByConstraint != null)
				crdtOperation.setParentsMap(parentsByConstraint);

		} else
			// its a "neutral" or "parent" row
			crdtOperation.setOpType(CRDTOperationType.INSERT);
	}

	private Row findParent(Row childRow, ForeignKeyConstraint constraint, SQLInterface sqlInterface) throws
			SQLException
	{
		String query = QueryCreator.findParent(childRow, constraint, this.sandboxId);

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
		}
		else //create new symbol entry
		{
			SymbolEntry symbolEntry = new SymbolEntry();
			symbolEntry.setSymbol(symbol);
			symbolEntry.setRequiresCoordination(false);
			symbolsMap.put(symbol, symbolEntry);
			linkSymbolToField(op, symbolEntry, dataField);
		}

		if(dataField.getSemantic() == SemanticPolicy.SEMANTIC)
		{
			SymbolEntry symbolEntry = symbolsMap.get(symbol);
			symbolEntry.setRequiresCoordination(true);

			AutoIncrementConstraint autoIncrementConstraint = this.databaseTable.getAutoIncrementConstraint(
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