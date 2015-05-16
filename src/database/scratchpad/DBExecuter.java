package database.scratchpad;


import database.constraints.Constraint;
import database.constraints.ConstraintType;
import database.constraints.check.CheckConstraint;
import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.jdbc.Result;
import database.jdbc.util.DBUpdateResult;
import database.util.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
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
import runtime.operation.*;
import util.exception.CheckConstraintViolatedException;
import util.defaults.Configuration;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.debug.Debug;
import util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.util.*;


public class DBExecuter implements IExecuter
{

	private static final Logger LOG = LoggerFactory.getLogger(DBExecuter.class);
	private static final String SP_DELETED_EXPRESSION = ScratchpadDefaults.SCRATCHPAD_COL_DELETED + "=0";

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
	private List<Operation> ops;

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
	 * Returns the alias table name
	 */
	public String getAliasTable()
	{
		return tableDefinition.getNameAlias();
	}

	/**
	 * Add deleted to where statement
	 */
	public void addDeletedKeysWhere(StringBuffer buffer)
	{

		/*
		for(String[] pk : deletedPks)
		{
			String[] pkAlias = tableDefinition.getPksAlias();
			for(int i = 0; i < pk.length; i++)
			{
				buffer.append(" and ");
				buffer.append(pkAlias[i]);
				buffer.append(" <> '");
				buffer.append(pk[i]);
				buffer.append("'");
			}
		}      */
	}

	/**
	 * Returns what should be in the from clause in select statements plus the primary key
	 */
	public void addFromTablePlusPrimaryKeyValues(StringBuffer buffer, boolean both, String[] tableNames,
												 String whereClauseStr)
	{
		if(both && modified)
		{
			StringBuilder pkValueStrBuilder = new StringBuilder("");
			String[] subExpressionStrs = null;
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
						LOG.trace("I identified one primary key from your where clause " + pk);
						if(subExpressionStrs[i].contains("="))
						{
							String tempStr = subExpressionStrs[i].replaceAll("\\s+", "");
							LOG.trace("I remove all space " + tempStr);
							int indexOfEqualSign = tempStr.indexOf('=');
							if(indexOfEqualSign < tempStr.length() - 1)
							{
								String valuePart = tempStr.substring(indexOfEqualSign + 1);
								if(this.isInteger(valuePart))
								{
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
	public void addKeyVVBothTable(StringBuffer buffer, String tableAlias)
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
	protected void addWhere(StringBuffer buffer, Expression e)
	{
		if(e == null)
			return;
		buffer.append(" WHERE (");
		buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_DELETED);
		buffer.append(" = FALSE)");
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
			String tRep = null;
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
	protected void addWhere(StringBuffer buffer, Expression e, IExecuter[] policies, String[][] tables,
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
			buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_DELETED);
			buffer.append(" = FALSE");
		}
		if(e != null)
		{
			buffer.append(" and ( ");
			String where = replaceAliasInStr(e.toString(), policies, tables, inTempTable);
			buffer.append(where);
			buffer.append(" ) ");
		}
		addDeletedKeysWhere(buffer);
	}

	@Override
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

			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("DROP TABLE IF EXISTS ");
			StringBuffer buffer = new StringBuffer();

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append("CREATE LOCAL TEMPORARY TABLE ");    // for H2
			else
				buffer.append("CREATE TABLE IF NOT EXISTS ");        // for mysql

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
				if(colSet.getString(4).equalsIgnoreCase(ScratchpadDefaults.SCRATCHPAD_COL_DELETED))
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
				LOG.trace("UNIQUE INDEX " + columnName);
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

			LOG.trace("Unique indices: " + Arrays.toString(uqIndicesPlain));

			this.tableDefinition = new TableDefinition(this.databaseTable.getName(), tableNameAlias, this.tableId,
					colsIsStr, cols, aliasCols, tempAliasCols, pkPlain, pkAlias, pkTempAlias, uqIndicesPlain);

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append(") NOT PERSISTENT;");    // FOR H2
			else
				buffer.append(") ENGINE=MEMORY;");    // FOR MYSQL

			LOG.trace("buffer1: {}", buffer.toString());
			LOG.trace("buffer2: {}", buffer2.toString());
			scratchpad.executeUpdate(buffer2.toString());
			scratchpad.executeUpdate(buffer.toString());

		} catch(SQLException e)
		{
			LOG.error("failed to create temporary tables for scratchpad {}", scratchpad.getScratchpadId());
			RuntimeHelper.throwRunTimeException("scratchpad creation failed", ExitCode.SCRATCHPAD_INIT_FAILED);
		}
		LOG.trace("executor for table {} created", this.databaseTable.getName());
	}

	@Override
	public ResultSet executeTemporaryQueryOnSingleTable(Select selectOp, IDBScratchPad db)
			throws SQLException, ScratchpadException
	{
		LOG.trace("creating selection for query {}", selectOp.toString());
		String queryToOrigin;
		String queryToTemp;

		StringBuffer buffer = new StringBuffer();

		PlainSelect plainSelect = (PlainSelect) selectOp.getSelectBody();

		if(plainSelect.isForUpdate())
			plainSelect.setForUpdate(false);

		List columnsToFetch = plainSelect.getSelectItems();

		if(columnsToFetch.size() == 1 && columnsToFetch.get(0).toString().equalsIgnoreCase("*"))
			plainSelect.setSelectItems(this.selectAllItems);

		StringBuffer whereClauseTemp = new StringBuffer();

		if(plainSelect.getWhere() != null)
		{
			whereClauseTemp.append("(");
			whereClauseTemp.append(plainSelect.getWhere());
			whereClauseTemp.append(") AND (");
		}

		whereClauseTemp.append(SP_DELETED_EXPRESSION);

		if(plainSelect.getWhere() != null)
			whereClauseTemp.append(")");

		StringBuffer whereClauseOrig = new StringBuffer(whereClauseTemp);

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
		LOG.trace("query generated: {}", finalQuery);

		return db.executeQuery(finalQuery);
	}

	private ResultSet cenas(IDBScratchPad pad, String cenas) throws SQLException
	{
		return pad.executeQuery(cenas);
	}

	@Override
	public ResultSet executeTemporaryQueryOnMultTable(Select selectOp, IDBScratchPad db, IExecuter[] policies,
													  String[][] tables) throws SQLException, ScratchpadException
	{
		if(true)
			return cenas(db, selectOp.toString());
		else
		{
			Debug.println("multi table select >>" + selectOp);
			HashMap<String, Integer> columnNamesToNumbersMap = new HashMap<>();
			StringBuffer buffer = new StringBuffer();
			buffer.append("select ");                        // select in base table
			PlainSelect select = (PlainSelect) selectOp.getSelectBody();
			List what = select.getSelectItems();
			int colIndex = 1;
			TableDefinition tabdef;
			boolean needComma = true;
			boolean aggregateQuery = false;
			if(what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*"))
			{
				//			buffer.append( "*");
				for(int i = 0; i < policies.length; i++)
				{
					tabdef = policies[i].getTableDefinition();
					tabdef.addAliasColumnList(buffer, tables[i][1]);
					for(int j = 0; j < tabdef.colsPlain.length - 3; j++)
					{ //columns doesnt include scratchpad tables
						columnNamesToNumbersMap.put(tabdef.colsPlain[j], colIndex);
						columnNamesToNumbersMap.put(tabdef.name + "." + tabdef.colsPlain[j], colIndex++);
					}
				}
				needComma = false;
			} else
			{
				Iterator it = what.iterator();
				String str;
				boolean f = true;
				while(it.hasNext())
				{
					if(f)
						f = false;
					else
						buffer.append(",");
					str = it.next().toString();
					if(str.startsWith("COUNT(") || str.startsWith("count(") || str.startsWith("MAX(") || str
							.startsWith(

							"max("))
						aggregateQuery = true;
					int starPos = str.indexOf(".*");
					if(starPos != -1)
					{
						String itTable = str.substring(0, starPos).trim();
						for(int i = 0; i < tables.length; )
						{
							if(itTable.equalsIgnoreCase(tables[i][0]) || itTable.equalsIgnoreCase(tables[i][1]))
							{
								tabdef = policies[i].getTableDefinition();
								tabdef.addAliasColumnList(buffer, tables[i][1]);
								for(int j = 0; j < tabdef.colsPlain.length - 3; j++)
								{ //columns doesnt include scratchpad tables
									columnNamesToNumbersMap.put(tabdef.colsPlain[j], colIndex);
									columnNamesToNumbersMap.put(tabdef.name + "." + tabdef.colsPlain[j], colIndex++);
								}
								break;
							}
							i++;
							if(i == tables.length)
							{
								Debug.println("not expected " + str + " in select");
								buffer.append(str);
							}
						}
						f = true;
						needComma = false;
					} else
					{
						buffer.append(str);
						int aliasindex = str.toUpperCase().indexOf(" AS ");
						if(aliasindex != -1)
						{
							columnNamesToNumbersMap.put(str.substring(aliasindex + 4), colIndex++);
						} else
						{
							int dotindex;
							dotindex = str.indexOf(".");
							if(dotindex != -1)
							{
								columnNamesToNumbersMap.put(str.substring(dotindex + 1), colIndex++);
							} else
							{
								columnNamesToNumbersMap.put(str, colIndex++);

							}//else
						}
						needComma = true;
					}//else

				}
			}
			if(!aggregateQuery)
			{
				for(int i = 0; i < policies.length; i++)
				{
					if(needComma)
						buffer.append(",");
					else
						needComma = true;
					policies[i].addKeyVVBothTable(buffer, tables[i][1]);
				}
			}
			buffer.append(" from ");
			//get all joins:
			if(select.getJoins() != null)
			{
				String whereConditionStr = select.getWhere().toString();
				for(int i = 0; i < policies.length; i++)
				{
					if(i > 0)
						buffer.append(",");
					policies[i].addFromTablePlusPrimaryKeyValues(buffer, !db.isReadOnly(), tables[i],
							whereConditionStr);
					//policies[i].addFromTable( buffer, ! db.isReadOnly(), tables[i]);
				}
			/*for(Iterator joinsIt = select.getJoins().iterator();joinsIt.hasNext();){
				Join join= (Join) joinsIt.next();
				String joinString = join.toString();
				if(joinString.contains("inner join")||joinString.contains("INNER JOIN")|| joinString.contains("left
				outer join") || joinString.contains("LEFT OUTER JOIN"))
					buffer.append(" ");
				else
					buffer.append(",");
				buffer.append(join.toString());
			}*/
			}

			//		}else{
			//			for( int i = 0; i < policies.length; i++) {
			//				if( i > 0)
			//					buffer.append( ",");
			//				policies[i].addFromTable( buffer, ! db.isReadOnly(), tables[i]);
			//			}
			//		}
			addWhere(buffer, select.getWhere(), policies, tables, false);
			return db.executeQuery(buffer.toString());
		}
	}

	@Override
	public Result executeTemporaryUpdate(DBSingleOperation dbOp, IDBScratchPad db)
			throws SQLException, ScratchpadException
	{
		modified = true;
		if(dbOp.getStatement() instanceof Insert)
			return executeTempOpInsert((Insert) dbOp.getStatement(), db);
		else if(dbOp.getStatement() instanceof Delete)
			return executeTempOpDelete((Delete) dbOp.getStatement(), db);
		else if(dbOp.getStatement() instanceof Update)
			return executeTempOpUpdate((Update) dbOp.getStatement(), db);
		else
		{
			modified = false;
			RuntimeHelper.throwRunTimeException("query operation expected", ExitCode.UNEXPECTED_OP);
		}
		return null;
	}

	@Override
	public void resetExecuter(IDBScratchPad pad) throws SQLException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("TRUNCATE TABLE ");
		buffer.append(this.tempTableName);
		pad.addToBatchUpdate(buffer.toString());
		this.duplicatedRows.clear();
		this.recordedPkValues.clear();
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
	 * @throws SQLException
	 */
	protected DBUpdateResult executeTempOpInsert(Insert insertOp, IDBScratchPad db) throws SQLException
	{
		StringBuffer buffer = new StringBuffer();
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
				FieldValue newContentField = new FieldValue(field, val);
				insertedRow.addFieldValue(newContentField);

				if(field.isPrimaryKey())
					pkValue.addFieldValue(newContentField);

				if(!first)
					buffer.append(",");

				first = false;
				buffer.append(col);

				for(Constraint c : field.getInvariants())
				{
					if(c.getType() == ConstraintType.CHECK || c.getType() == ConstraintType.UNIQUE)
						insertedRow.addConstraintToverify(c);
				}
			}
			buffer.append(")");
		}

		Operation op;

		if(this.fkConstraints.size() > 0) // its a child row
		{
			List<Row> parents = DatabaseCommon.findParentRows(insertedRow, this.fkConstraints, db);
			op = new InsertChildOperation(db.getActiveTransaction().getNextOperationId(),
					this.databaseTable.getExecutionPolicy(), parents, insertedRow);
		} else // its a "neutral" or "parent" row
			op = new InsertOperation(db.getActiveTransaction().getNextOperationId(),
					this.databaseTable.getExecutionPolicy(), insertedRow);

		db.getActiveTransaction().addOperation(op);

		buffer.append(" values ");
		buffer.append(insertOp.getItemsList());

		int result = db.executeUpdate(buffer.toString());
		this.recordedPkValues.put(pkValue.getValue(), pkValue);

		LOG.trace("new insert: {}", buffer.toString());
		return DBUpdateResult.createResult(result);
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
	 * @throws SQLException
	 */
	private DBUpdateResult executeTempOpDelete(Delete deleteOp, IDBScratchPad db) throws SQLException
	{
		LOG.trace("fetching rows to delete");
		StringBuffer buffer = new StringBuffer();
		buffer.append("(SELECT *");
		//buffer.append(this.pk.getQueryClause());
		buffer.append(" FROM ");
		buffer.append(deleteOp.getTable().toString());
		addWhere(buffer, deleteOp.getWhere());
		if(this.duplicatedRows.size() > 0 || this.deletedRows.size() > 0)
		{
			buffer.append("AND ");
			this.generateNotInDeletedAndUpdatedClause(buffer);
		}
		buffer.append(") UNION (SELECT *");
		//buffer.append(this.pk.getQueryClause());
		buffer.append(" FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, deleteOp.getWhere());
		buffer.append(")");
		String query = buffer.toString();

		LOG.trace("selection for delete: {}", query);

		int rowsDeleted = 1;
		ResultSet res = null;

		try
		{
			res = db.executeQuery(query);
			Row rowToDelete = null;
			while(res.next())
			{
				if(!res.isLast())
					RuntimeHelper.throwRunTimeException("ResultSet should contain exactly 1 row",
							ExitCode.FETCH_RESULTS_ERROR);

				rowsDeleted++;
				rowToDelete = DatabaseCommon.getFullRow(res, this.databaseTable);

				this.recordedPkValues.put(rowToDelete.getPrimaryKeyValue().getValue(),
						rowToDelete.getPrimaryKeyValue());
			}

			buffer = new StringBuffer();
			buffer.append("delete from ");
			buffer.append(this.tempTableName);
			buffer.append(" where ");
			buffer.append(deleteOp.getWhere().toString());
			buffer.append(";");
			String delete = buffer.toString();

			db.executeUpdate(delete);

			Operation op;
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

			return DBUpdateResult.createResult(rowsDeleted);
		} catch(SQLException e)
		{
			throw e;
		} finally
		{
			DbUtils.closeQuietly(res);
		}
	}

	private DBUpdateResult executeTempOpUpdate(Update updateOp, IDBScratchPad db) throws SQLException
	{
		// before writting in the scratchpad, add the missing rows to the scratchpad
		this.addMissingRowsToScratchpad(updateOp, db);
		Row updatedRow = this.getUpdatedRowFromDatabase(updateOp, db);

		// now perform the actual update only in the scratchpad
		StringBuffer buffer = new StringBuffer();
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

			//FIXME: currently, we do not allow primary keys, immutable fields and fields that are pointing to
			// some parent be modified
			if(field.isImmutableField() || field.isPrimaryKey() || field.hasParent())
				RuntimeHelper.throwRunTimeException("trying to modify a primary key or immutable field",
						ExitCode.UNEXPECTED_OP);

			if(newValue == null)
				newValue = "NULL";

			FieldValue newFieldValue;

			if(field.isDeltaField())
				newFieldValue = new DeltaFieldValue(field, newValue, updatedRow.getFieldValue(field.getFieldName()).getValue());
			else
				newFieldValue = new FieldValue(field, newValue);

			updatedRow.updateFieldValue(newFieldValue);

			for(Constraint c : field.getInvariants())
			{
				if(c.getType() == ConstraintType.CHECK || c.getType() == ConstraintType.UNIQUE)
					updatedRow.addConstraintToverify(c);
			}

			buffer.append(columnName);
			buffer.append("=");
			buffer.append(newFieldValue.getFormattedValue());
			if(colIt.hasNext())
				buffer.append(",");
		}

		Operation op;
		int affectedRows = 1;

		// if is parent table, check if this op has side effects
		if(this.databaseTable.isParentTable() && updatedRow.hasSideEffects())
		{
			op = new UpdateParentOperation(db.getActiveTransaction().getNextOperationId(),
					this.databaseTable.getExecutionPolicy(), updatedRow);
			this.calculateOperationSideEffects((UpdateParentOperation) op, updatedRow, db);
			affectedRows += ((UpdateParentOperation) op).getNumberOfRows();

		} else // if a parent row update has no side effects its the same as update neutral row
			op = new UpdateOperation(db.getActiveTransaction().getNextOperationId(),
					this.databaseTable.getExecutionPolicy(), updatedRow);

		db.getActiveTransaction().addOperation(op);

		buffer.append(" WHERE ");
		buffer.append(updateOp.getWhere().toString());
		String updateStr = buffer.toString();
		LOG.trace("transformed update: {}", updateStr);
		db.addToBatchUpdate(updateStr);
		db.executeBatch();
		this.modified = true;

		return DBUpdateResult.createResult(affectedRows);
	}

	/**
	 * Inserts missing rows in the temporary table and returns the list of rows
	 * This must be done before updating rows in the scratchpad.
	 *
	 * @param updateOp
	 * @param pad
	 *
	 * @throws SQLException
	 */
	private void addMissingRowsToScratchpad(Update updateOp, IDBScratchPad pad) throws SQLException
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("(SELECT *, '" + updateOp.getTables().get(0).toString() + "' as tname FROM ");
		buffer.append(updateOp.getTables().get(0).toString());
		addWhere(buffer, updateOp.getWhere());
		//buffer.append(") UNION (select *, '" + this.tempTableName + "' as tname FROM ");
		buffer.append(" AND _del=0) UNION (select *, '" + this.tempTableName + "' as tname FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, updateOp.getWhere());
		//buffer.append(");");
		buffer.append(" AND _del=0);");

		ResultSet res = null;
		try
		{
			res = pad.executeQuery(buffer.toString());
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

				Iterator<DataField> fieldsIt = this.fields.values().iterator();

				while(fieldsIt.hasNext())
				{
					DataField field = fieldsIt.next();
					Object oldContent;

					if(!field.isDeletedFlagField())
						oldContent = res.getObject(field.getFieldName());
					else
					{
						oldContent = Integer.toString(res.getInt(field.getFieldName()));
						oldContent = Boolean.parseBoolean(oldContent.toString());
					}

					if(oldContent == null)
						oldContent = "NULL";

					if(field.isStringField())
					{
						buffer.append("'");
						buffer.append(oldContent);
						buffer.append("'");
					} else
						buffer.append(oldContent);

					if(fieldsIt.hasNext())
						buffer.append(",");
				}

				buffer.append(")");
				pad.executeUpdate(buffer.toString());
			}
		} catch(SQLException e)
		{
			throw e;
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

	private void verifyCheckConstraints(DataField field, String newValue) throws CheckConstraintViolatedException
	{
		for(Constraint constraint : field.getInvariants())
		{
			if(constraint instanceof CheckConstraint)
				if(!((CheckConstraint) constraint).isValidValue(newValue))
					throw new CheckConstraintViolatedException(
							"check constraint violated for field " + field.getFieldName());
		}
	}

	private Row getUpdatedRowFromDatabase(Update updateOp, IDBScratchPad pad) throws SQLException
	{
		Expression whereClause = updateOp.getWhere();
		if(whereClause == null)
			RuntimeHelper.throwRunTimeException("update operation should specify a primary key in the where clause",
					ExitCode.INVALIDUSAGE);

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT *");
		buffer.append(" from ");
		buffer.append(this.tempTableName);
		buffer.append(" where ");
		buffer.append(whereClause);

		ResultSet rs = null;
		try
		{
			rs = pad.executeQuery(buffer.toString());
			rs.next();

			if(!rs.isLast())
				RuntimeHelper.throwRunTimeException("ResultSet should contain exactly 1 row",
						ExitCode.FETCH_RESULTS_ERROR);

			Row row = DatabaseCommon.getFullRow(rs, this.databaseTable);
			if(row != null)
				return row;

			RuntimeHelper.throwRunTimeException("error fetching updated row from database",
					ExitCode.FETCH_RESULTS_ERROR);

			return null;

		} catch(SQLException e)
		{
			throw e;
		} finally
		{
			DbUtils.closeQuietly(rs);
		}
	}

	private void generateNotInDeletedAndUpdatedClause(StringBuffer buffer)
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

	private void calculateOperationSideEffects(ParentOperation op, Row parentRow, IDBScratchPad pad) throws
			SQLException
	{

		for(ForeignKeyConstraint fkConstraint : parentRow.getTable().getChildTablesConstraints())
		{

			List<Row> childRows = DatabaseCommon.findChildsFromTable(parentRow, fkConstraint.getChildTable(),
					fkConstraint.getFieldsRelations(), pad);

			if(op.getOperationType() == OperationType.DELETE && fkConstraint.getPolicy().getDeleteAction() ==
					ForeignKeyAction.RESTRICT)
				if(childRows.size() > 0)
					throw new SQLException("cannot delete parent row because of foreign key restriction");

			if(op.getOperationType() == OperationType.UPDATE && fkConstraint.getPolicy().getUpdateAction() == ForeignKeyAction.RESTRICT)
				if(childRows.size() > 0)
					throw new SQLException("cannot update parent row because of foreign key restriction");

			op.addSideEffects(fkConstraint, childRows);
		}

	}

}