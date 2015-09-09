package database.scratchpad;


import database.constraints.Constraint;
import database.constraints.ConstraintType;
import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.util.DatabaseCommon;
import database.util.PrimaryKey;
import database.util.PrimaryKeyValue;
import database.util.Row;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import database.util.value.DeltaFieldValue;
import database.util.value.FieldValue;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import nodes.proxy.IProxyNetwork;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import runtime.Transaction;
import runtime.operation.*;
import util.ExitCode;
import util.defaults.Configuration;
import util.defaults.DBDefaults;
import util.defaults.ScratchpadDefaults;

import java.io.StringReader;
import java.sql.*;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ImprovedScratchPad implements IDBScratchPad
{

	private static final Logger LOG = LoggerFactory.getLogger(ImprovedScratchPad.class);

	private Transaction activeTransaction;
	private int id;
	private Map<String, DBExecuter> executers;
	private final CCJSqlParserManager parser;
	private Connection defaultConnection;
	private Statement statQ;
	private Statement statU;
	private Statement statBU;
	private boolean batchEmpty;
	private String defaultDate;

	public ImprovedScratchPad(int id, Connection dbConnection, CCJSqlParserManager parser)
			throws SQLException, ScratchpadException
	{
		this.id = id;
		this.defaultConnection = dbConnection;
		this.executers = new HashMap<>();
		this.batchEmpty = true;
		this.parser = parser;
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();

		java.util.Date dt = new java.util.Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		this.defaultDate = sdf.format(dt);

		this.createDBExecuters();
	}

	@Override
	public void startTransaction(int txnId)
	{
		try
		{
			this.resetScratchpad();
		} catch(SQLException e)
		{
			LOG.error("failed to clean scratchpad before starting transaction: {}", e.getMessage());
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_CLEANUP_ERROR);
		}
		this.activeTransaction = new Transaction(txnId);

		if(LOG.isTraceEnabled())
			LOG.trace("Beggining txn {}", activeTransaction.getTxnId());
	}

	@Override
	public void commitTransaction(IProxyNetwork network) throws SQLException
	{
		RuntimeUtils.throwRunTimeException("missing method implementation", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public int getScratchpadId()
	{
		return this.id;
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		net.sf.jsqlparser.statement.Statement statement;

		try
		{
			statement = this.parser.parse(new StringReader(op));

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(!(statement instanceof Select))
			throw new SQLException("query statement expected");

		Select selectStatement = (Select) statement;

		SelectBody sb = selectStatement.getSelectBody();

		if(!(sb instanceof PlainSelect))
			throw new SQLException("Cannot process select : " + selectStatement.toString());

		PlainSelect psb = (PlainSelect) sb;
		FromItem fi = psb.getFromItem();

		if(!(fi instanceof Table))
			throw new RuntimeException("Cannot process select : " + selectStatement.toString());

		List joins = psb.getJoins();
		int nJoins = joins == null ? 0 : joins.size();

		String tableName;
		if(nJoins == 0)
		{
			tableName = ((Table) fi).getName().toUpperCase();

			DBExecuter executor = this.executers.get(tableName);

			if(executor == null)
			{
				LOG.error("executor for table {} not found", tableName);
				throw new SQLException("executor not found");
			} else
				return executor.executeTemporaryQueryOnSingleTable(selectStatement, this);
		} else
			//TODO: implement multi-table queries (joins)
			return executeQueryMainStorage(op);
	}

	@Override
	public ResultSet executeQueryMainStorage(String op) throws SQLException
	{
		return this.statQ.executeQuery(op);
	}

	@Override
	public int executeUpdateMainStorage(String op) throws SQLException
	{
		return this.statU.executeUpdate(op);
	}

	@Override
	public void addToBatchUpdate(String op) throws SQLException
	{
		this.statBU.addBatch(op);
		this.batchEmpty = false;
	}

	@Override
	public int executeBatch() throws SQLException
	{
		if(this.batchEmpty)
		{
			LOG.warn("trying to executeUpdate an empty batch for pad {}", this.getScratchpadId());
			// -1 is the default error
			return -1;
		}

		int[] res = statBU.executeBatch();
		int finalResult = 0;

		for(int i = 0; i < res.length; i++)
			finalResult += res[i];

		statBU.clearBatch();
		batchEmpty = true;
		return finalResult;
	}

	@Override
	public Transaction getActiveTransaction()
	{
		return this.activeTransaction;
	}

	private void createDBExecuters() throws SQLException
	{
		DatabaseMetaData metadata = this.defaultConnection.getMetaData();
		String[] types = {"TABLE"};
		ResultSet tblSet = metadata.getTables(null, null, "%", types);

		ArrayList<String> tempTables = new ArrayList<>();
		while(tblSet.next())
		{
			String tableName = tblSet.getString(3);
			if(tableName.startsWith(ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX))
				continue;

			tempTables.add(tableName);
		}

		Collections.sort(tempTables);

		for(int i = 0; i < tempTables.size(); i++)
		{
			String tableName = tempTables.get(i);
			DBExecuter executor = new DBExecuter(i, tableName);
			executor.setup(metadata, this);
			this.executers.put(tableName.toUpperCase(), executor);
			this.defaultConnection.commit();
		}
	}

	public int executeUpdate(String op) throws SQLException
	{
		net.sf.jsqlparser.statement.Statement statement;

		try
		{
			statement = this.parser.parse(new StringReader(op));

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(statement instanceof Select)
			throw new SQLException("update operation expected but instead we got a select");

		String tableName = getTableName(statement);

		DBExecuter executor = this.executers.get(tableName);

		if(executor == null)
		{
			LOG.error("executor for table {} not found", tableName);
			throw new SQLException("executor not found");
		} else
			return executor.executeTemporaryUpdate(statement, this);

	}

	private void resetScratchpad() throws SQLException
	{
		this.activeTransaction = null;
		this.statBU.clearBatch();

		for(DBExecuter executer : this.executers.values())
			executer.resetExecuter(this);

		this.executeBatch();
		this.batchEmpty = true;
	}

	private String getTableName(net.sf.jsqlparser.statement.Statement statement) throws SQLException
	{
		if(statement instanceof Insert)
			return ((Insert) statement).getTable().getName().toUpperCase();
		if(statement instanceof Delete)
			return ((Delete) statement).getTable().getName().toUpperCase();
		if(statement instanceof Update)
		{
			if(((Update) statement).getTables().size() != 1)
				throw new SQLException("multi-table update operation not expected");
			return ((Update) statement).getTables().get(0).getName().toUpperCase();
		} else
			throw new SQLException("could not parse statement table name");
	}

	private class DBExecuter
	{

		private static final String SP_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=0";

		private TableDefinition tableDefinition;
		private DatabaseTable databaseTable;
		private List<ForeignKeyConstraint> fkConstraints;
		private Map<String, DataField> fields;
		private String tempTableName;
		private String tempTableNameAlias;
		private int tableId;
		private boolean modified;
		private FromItem fromItemTemp;
		private PrimaryKey pk;
		private List<SelectItem> selectAllItems;
		private List<ShadowOperation> ops;

		private Set<PrimaryKeyValue> duplicatedRows;
		private Set<PrimaryKeyValue> deletedRows;
		private Map<String, PrimaryKeyValue> recordedPkValues;

		public DBExecuter(int tableId, String tableName)
		{
			this.tableId = tableId;
			this.ops = new ArrayList<>();
			this.fkConstraints = new ArrayList<>();

			this.databaseTable = Configuration.getInstance().getDatabaseMetadata().getTable(tableName);
			for(Constraint c : this.databaseTable.getTableInvarists())
			{
				if(c instanceof ForeignKeyConstraint)
					this.fkConstraints.add((ForeignKeyConstraint) c);
			}

			this.pk = databaseTable.getPrimaryKey();
			this.modified = false;
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
		}

		/**
		 * Returns the table definition for this execution policy
		 */
		public TableDefinition getTableDefinition()
		{
			return tableDefinition;
		}

		/**
		 * Returns the temporary table name
		 */
		public String getTempTableName()
		{
			return tempTableName;
		}

		/**
		 * Returns the table name
		 */
		public String getTableName()
		{
			return tableDefinition.name;
		}

		/**
		 * Returns what should be in the from clause in select statements plus the primary key
		 */
		public void addFromTablePlusPrimaryKeyValues(StringBuilder buffer, boolean both, String[] tableNames,
													 String whereClauseStr)
		{
			if(both && modified)
			{
				StringBuilder pkValueStrBuilder = new StringBuilder("");
				String[] subExpressionStrs;
				if(whereClauseStr.contains("AND"))
				{
					subExpressionStrs = whereClauseStr.split("AND");
				} else
				{
					subExpressionStrs = whereClauseStr.split("and");
				}

				boolean isFirst = true;
				for(int i = 0; i < subExpressionStrs.length; i++)
				{
					for(int j = 0; j < tableDefinition.getPksPlain().length; j++)
					{
						String pk = tableDefinition.getPksPlain()[j];
						if(subExpressionStrs[i].contains(pk))
						{
							if(LOG.isTraceEnabled())
								LOG.trace("I identified one primary key from your where clause " + pk);
							if(subExpressionStrs[i].contains("="))
							{
								String tempStr = subExpressionStrs[i].replaceAll("\\s+", "");
								if(LOG.isTraceEnabled())
									LOG.trace("I remove all space " + tempStr);
								int indexOfEqualSign = tempStr.indexOf('=');
								if(indexOfEqualSign < tempStr.length() - 1)
								{
									String valuePart = tempStr.substring(indexOfEqualSign + 1);
									if(this.isInteger(valuePart))
									{
										if(LOG.isTraceEnabled())
											LOG.trace("We identified an integer");
										if(!isFirst)
										{
											pkValueStrBuilder.append("AND");
										} else
										{
											isFirst = false;
										}
										pkValueStrBuilder.append(tempStr.subSequence(0, indexOfEqualSign));
										pkValueStrBuilder.append("=");
										pkValueStrBuilder.append(valuePart);
									}
								}
							}
						}
					}
				}

				buffer.append("(select * from ");
				buffer.append(tableNames[0]);
				if(!pkValueStrBuilder.toString().equals(""))
				{
					buffer.append(" where ");
					buffer.append(pkValueStrBuilder.toString());
				}
				buffer.append(" union select * from ");
				buffer.append(tempTableName);
				if(!pkValueStrBuilder.toString().equals(""))
				{
					buffer.append(" where ");
					buffer.append(pkValueStrBuilder.toString());
				}
				buffer.append(") as ");
				buffer.append(tableNames[1]);
			} else
			{
				buffer.append(tableDefinition.name);
				buffer.append(" as ");
				buffer.append(tableNames[1]);
			}
		}

		/**
		 * Returns what should be in the what clause in select statements
		 */
		public void addKeyVVBothTable(StringBuilder buffer, String tableAlias)
		{
			//		return tableDefinition.getPkListAlias() + "," + tableDefinition.getNameAlias() + "." +
			// ScratchpadDefaults.SCRATCHPAD_COL_CLOCK;
			String[] pks = tableDefinition.getPksPlain();
			for(int i = 0; i < pks.length; i++)
			{
				buffer.append(tableAlias);
				buffer.append(".");
				buffer.append(pks[i]);
				buffer.append(",");
			}
			buffer.append(tableAlias);
			buffer.append(".");
			buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_CLOCK);
		}

		/**
		 * Add where clause to the current buffer, removing needed deleted Pks
		 */
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

		/**
		 * Replace alias in string
		 *
		 * @param where
		 * @param policies
		 * @param tables
		 *
		 * @return
		 */
		protected String replaceAliasInStr(String where, IExecuter[] policies, String[][] tables, boolean inTempTable)
		{
			for(int i = 0; i < tables.length; i++)
			{
				String t = "([ ,])" + tables[i][1] + "\\.";
				String tRep;
				if(inTempTable)
				{
					tRep = "$1" + ((DBExecuter) policies[i]).getTempTableName() + ".";
					//else
					//tRep = policies[i].getName() + ".";
					where = where.replaceAll(t, tRep);
				}
			}
			return where;
/*		StringBuffer b = new StringBuffer();
		for( int i = 0; i < tables.length; i++) {
			String t = tables[i][1] + ".";
			String tRep = policies[i].getAliasTable() + ".";
			String whereUp = where.toUpperCase();
			int pos = 0;
			b.setLength(0);
			while( true) {
				int nextPos = whereUp.indexOf( t,pos);
				if( nextPos < 0) {
					b.append( where.substring( pos));
					break;
				}
				if( nextPos > 0)
					b.append( where.substring(pos, nextPos));
				if( nextPos == 0) {
					b.append( tRep);
					pos = nextPos + t.length();
				} else {
					char ch = where.charAt(nextPos - 1);
					if( Character.isJavaIdentifierPart(ch))	{	// not completely correct, but should work
						b.append( t);
						pos = nextPos + t.length();
					} else {
						b.append( tRep);
						pos = nextPos + t.length();
					}
				}
			}
			where = b.toString();
		}
		return where;
*/
		}

		/**
		 * Add where clause to the current buffer, removing needed deleted Pks
		 */
		protected void addWhere(StringBuilder buffer, Expression e, IExecuter[] policies, String[][] tables,
								boolean inTempTable)
		{
			if(e == null)
				return;
			buffer.append(" where ");
			for(int i = 0; i < policies.length; i++)
			{
				if(i > 0)
					buffer.append(" and ");
				buffer.append(tables[i][1]);
				buffer.append(".");
				buffer.append(SP_DELETED_EXPRESSION);
			}
			if(e != null)
			{
				buffer.append(" and ( ");
				String where = replaceAliasInStr(e.toString(), policies, tables, inTempTable);
				buffer.append(where);
				buffer.append(" ) ");
			}
		}

		public void setup(DatabaseMetaData metadata, IDBScratchPad scratchpad)
		{
			try
			{
				this.tempTableName = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.databaseTable.getName() +
						"_" +
						scratchpad.getScratchpadId();
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
					if(colSet.getString(4).equalsIgnoreCase(DBDefaults.DELETED_CLOCK_COLUMN))
					{
						buffer.append(" DEFAULT FALSE ");
					}

					temp.add(colSet.getString(4));
					tempAlias.add(tableNameAlias + "." + colSet.getString(4));
					tempTempAlias.add(this.tempTableNameAlias + "." + colSet.getString(4));
					tempIsStr.add(colSet.getInt(5) == Types.VARCHAR || colSet.getInt(
							5) == Types.LONGNVARCHAR || colSet.getInt(5) == Types.LONGVARCHAR || colSet.getInt(
							5) == Types.CHAR || colSet.getInt(5) == Types.DATE || colSet.getInt(
							5) == Types.TIMESTAMP || colSet.getInt(5) == Types.TIME);
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

				scratchpad.executeUpdateMainStorage(buffer2.toString());
				scratchpad.executeUpdateMainStorage(buffer.toString());

			} catch(SQLException e)
			{
				LOG.error("failed to create temporary tables for scratchpad", e);
				RuntimeUtils.throwRunTimeException("scratchpad creation failed", ExitCode.SCRATCHPAD_INIT_FAILED);
			}
			if(LOG.isTraceEnabled())
				LOG.trace("executor for table {} created", this.databaseTable.getName());
		}

		public ResultSet executeTemporaryQueryOnSingleTable(Select selectOp, IDBScratchPad db) throws SQLException
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

			return db.executeQueryMainStorage(finalQuery);
		}

		public int executeTemporaryUpdate(net.sf.jsqlparser.statement.Statement statement, IDBScratchPad db)
				throws SQLException
		{
			modified = true;

			if(statement instanceof Insert)
				return executeTempOpInsert((Insert) statement, db);
			else if(statement instanceof Delete)
				return executeTempOpDelete((Delete) statement, db);
			else if(statement instanceof Update)
				return executeTempOpUpdate((Update) statement, db);
			else
			{
				modified = false;
				throw new SQLException("update statement not found");
			}
		}

		public void resetExecuter(IDBScratchPad pad) throws SQLException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("TRUNCATE TABLE ");
			buffer.append(this.tempTableName);
			pad.addToBatchUpdate(buffer.toString());
			this.duplicatedRows.clear();
			this.recordedPkValues.clear();
			this.deletedRows.clear();
			this.ops.clear();
			this.modified = false;
		}

		/**
		 * Execute insert operation in the temporary table
		 *
		 * @param op
		 * @param insertOp
		 * @param db
		 *
		 * @return
		 *
		 * @throws java.sql.SQLException
		 */
		private int executeTempOpInsert(Insert insertOp, IDBScratchPad db) throws SQLException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("insert into ");
			buffer.append(tempTableName);

			List columnsList = insertOp.getColumns();
			List valuesList = ((ExpressionList) insertOp.getItemsList()).getExpressions();
			PrimaryKeyValue pkValue = new PrimaryKeyValue(this.databaseTable.getName());
			Row insertedRow = new Row(this.databaseTable, pkValue);

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

					if(!field.isHiddenField())
					{
						FieldValue newContentField = new FieldValue(field, val);
						insertedRow.updateFieldValue(newContentField);

						if(field.isPrimaryKey())
							pkValue.addFieldValue(newContentField);
					}

					if(!first)
						buffer.append(",");

					first = false;
					buffer.append(col);

					for(Constraint c : field.getInvariants())
					{
						if(c.requiresCoordination())
							if(c.getType() == ConstraintType.CHECK || c.getType() == ConstraintType.UNIQUE || c.getType() == ConstraintType.AUTO_INCREMENT)
								insertedRow.addConstraintToverify(c);
					}
				}

				buffer.append(")");
			}

			buffer.append(" values ");
			buffer.append(insertOp.getItemsList());

			int result = db.executeUpdateMainStorage(buffer.toString());

			ShadowOperation op;

			if(this.fkConstraints.size() > 0) // its a child row
			{
				Map<ForeignKeyConstraint, Row> parentsByConstraint = DatabaseCommon.findParentRows(insertedRow,
						this.fkConstraints, db);
				op = new InsertChildOperation(db.getActiveTransaction().getNextOperationId(),
						this.databaseTable.getExecutionPolicy(), parentsByConstraint, insertedRow);
			} else // its a "neutral" or "parent" row
				op = new InsertOperation(db.getActiveTransaction().getNextOperationId(),
						this.databaseTable.getExecutionPolicy(), insertedRow);

			db.getActiveTransaction().addOperation(op);

			this.recordedPkValues.put(insertedRow.getPrimaryKeyValue().getUniqueValue(), pkValue);

			return result;
		}

		/**
		 * My version of delete.
		 * We transform each delete in a simple query.
		 * We record the _SP_immut values of the ResultSet.
		 * Subsequent operations will ignore rows that match some _SP_immut value that we have recorded
		 *
		 * @param deleteOp
		 * @param db
		 *
		 * @return
		 *
		 * @throws java.sql.SQLException
		 */
		private int executeTempOpDelete(Delete deleteOp, IDBScratchPad db) throws SQLException
		{
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
				res = db.executeQueryMainStorage(query);
				Row rowToDelete = null;
				while(res.next())
				{
					if(!res.isLast())
						RuntimeUtils.throwRunTimeException("ResultSet should contain exactly 1 row",
								ExitCode.FETCH_RESULTS_ERROR);

					rowsDeleted++;
					PrimaryKeyValue rowPkValue = DatabaseCommon.getPrimaryKeyValue(res, this.databaseTable);
					rowToDelete = new Row(this.databaseTable, rowPkValue);
					DatabaseCommon.fillNormalFields(rowToDelete, res);

					this.recordedPkValues.remove(rowToDelete.getPrimaryKeyValue().getValue());
					this.duplicatedRows.remove(rowToDelete.getPrimaryKeyValue());
				}

				buffer = new StringBuilder();
				buffer.append("delete from ");
				buffer.append(this.tempTableName);
				buffer.append(" where ");
				buffer.append(deleteOp.getWhere().toString());
				String delete = buffer.toString();

				db.executeUpdateMainStorage(delete);

				ShadowOperation op;
				if(this.databaseTable.isParentTable())
				{
					op = new DeleteParentOperation(db.getActiveTransaction().getNextOperationId(),
							this.databaseTable.getExecutionPolicy(), rowToDelete);
					this.calculateOperationSideEffects((DeleteParentOperation) op, rowToDelete, db);
					rowsDeleted += ((DeleteParentOperation) op).getNumberOfRows();

				} else
					op = new DeleteOperation(db.getActiveTransaction().getNextOperationId(),
							this.databaseTable.getExecutionPolicy(), rowToDelete);

				db.getActiveTransaction().addOperation(op);

				return rowsDeleted;
			} finally
			{
				DbUtils.closeQuietly(res);
			}
		}

		private int executeTempOpUpdate(Update updateOp, IDBScratchPad db) throws SQLException
		{
			// before writting in the scratchpad, add the missing rows to the scratchpad
			this.addMissingRowsToScratchpad(updateOp, db);
			Row updatedRow = this.getUpdatedRowFromDatabase(updateOp, db);

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

				//FIXME: currently, we do not allow updates on primary keys, foreign keys and immutable fields
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

			ShadowOperation op;
			int affectedRows = 1;

			// if is parent table, check if this op has side effects
			if(this.databaseTable.isParentTable() && updatedRow.hasSideEffects())
			{
				op = new UpdateParentOperation(db.getActiveTransaction().getNextOperationId(),
						this.databaseTable.getExecutionPolicy(), updatedRow);
				this.calculateOperationSideEffects((UpdateParentOperation) op, updatedRow, db);
				affectedRows += ((UpdateParentOperation) op).getNumberOfRows();

			} else if(this.fkConstraints.size() > 0) // its a child row
			{
				Map<ForeignKeyConstraint, Row> parentsByConstraint = DatabaseCommon.findParentRows(updatedRow,
						this.fkConstraints, db);
				op = new UpdateChildOperation(db.getActiveTransaction().getNextOperationId(),
						this.databaseTable.getExecutionPolicy(), updatedRow, parentsByConstraint);
			} else
				op = new UpdateOperation(db.getActiveTransaction().getNextOperationId(),
						this.databaseTable.getExecutionPolicy(), updatedRow);

			db.getActiveTransaction().addOperation(op);

			buffer.append(" WHERE ");
			buffer.append(updateOp.getWhere().toString());
			String updateStr = buffer.toString();
			db.addToBatchUpdate(updateStr);
			db.executeBatch();
			this.modified = true;

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
		private void addMissingRowsToScratchpad(Update updateOp, IDBScratchPad pad) throws SQLException
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("(SELECT *, '" + updateOp.getTables().get(0).toString() + "' as tname FROM ");
			buffer.append(updateOp.getTables().get(0).toString());
			addWhere(buffer, updateOp.getWhere());
			//buffer.append(") UNION (select *, '" + this.tempTableName + "' as tname FROM ");
			buffer.append(" AND ");
			buffer.append(DBDefaults.DELETED_COLUMN);
			buffer.append("=0) UNION (select *, '" + this.tempTableName + "' as tname FROM ");
			buffer.append(this.tempTableName);
			addWhere(buffer, updateOp.getWhere());
			//buffer.append(");");
			buffer.append(" AND ");
			buffer.append(DBDefaults.DELETED_COLUMN);
			buffer.append("=0)");

			ResultSet res = null;
			try
			{
				res = pad.executeQueryMainStorage(buffer.toString());
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

					PrimaryKeyValue pkValue = new PrimaryKeyValue(this.getTableName());

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
								oldContent = defaultDate;

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
					pad.executeUpdateMainStorage(buffer.toString());
				}
			} finally
			{
				DbUtils.closeQuietly(res);
			}
		}

		//temporarily put there, need to file into other java files
		public boolean isInteger(String str)
		{
			if(str == null)
			{
				return false;
			}
			int length = str.length();
			if(length == 0)
			{
				return false;
			}
			int i = 0;
			if(str.charAt(0) == '-')
			{
				if(length == 1)
				{
					return false;
				}
				i = 1;
			}
			for(; i < length; i++)
			{
				char c = str.charAt(i);
				if(c <= '/' || c >= ':')
				{
					return false;
				}
			}
			return true;
		}

		private Row getUpdatedRowFromDatabase(Update updateOp, IDBScratchPad pad) throws SQLException
		{
			Expression whereClause = updateOp.getWhere();
			if(whereClause == null)
				RuntimeUtils.throwRunTimeException(
						"update operation should specify a primary key in the where " + "clause",
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
				rs = pad.executeQueryMainStorage(buffer.toString());

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

		private void calculateOperationSideEffects(ParentOperation op, Row parentRow, IDBScratchPad pad)
				throws SQLException
		{

			for(ForeignKeyConstraint fkConstraint : parentRow.getTable().getChildTablesConstraints())
			{

				List<Row> childRows = DatabaseCommon.findChildsFromTable(parentRow, fkConstraint.getChildTable(),
						fkConstraint.getFieldsRelations(), pad);

				if(op.getOperationType() == OperationType.DELETE && fkConstraint.getPolicy().getDeleteAction() == ForeignKeyAction.RESTRICT)
					if(childRows.size() > 0)
						throw new SQLException("cannot delete parent row because of foreign key restriction");

				if(op.getOperationType() == OperationType.UPDATE && fkConstraint.getPolicy().getUpdateAction() == ForeignKeyAction.RESTRICT)
					if(childRows.size() > 0)
						throw new SQLException("cannot update parent row because of foreign key restriction");

				op.addSideEffects(fkConstraint, childRows);
			}
		}
	}

}