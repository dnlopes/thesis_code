package database.scratchpad;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.jdbc.Result;
import database.jdbc.util.DBSelectResult;
import database.jdbc.util.DBUpdateResult;
import database.jdbc.util.DBWriteSetEntry;
import database.util.DataField;
import database.util.DatabaseTable;
import database.util.PrimaryKey;
import database.util.PrimaryKeyValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.txn.TableWriteSet;
import runtime.txn.TupleWriteSet;
import util.exception.CheckConstraintViolatedException;
import util.defaults.Configuration;
import runtime.RuntimeHelper;
import runtime.operation.DBSingleOperation;
import util.ExitCode;
import util.debug.Debug;
import util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.util.*;


public class DBExecuter implements IExecuter
{

	static final Logger LOG = LoggerFactory.getLogger(DBExecuter.class);

	private TableDefinition tableDefinition;
	private DatabaseTable databaseTable;
	private String tempTableName;
	private String tempTableNameAlias;
	private int tableId;
	private boolean modified;
	private FromItem fromItemTemp;
	private PrimaryKey pk;

	private List<String> selectAllItems;
	private Set<PrimaryKeyValue> duplicatedRows;
	private TableWriteSet writeSet;

	public DBExecuter(int tableId, String tableName)
	{
		this.tableId = tableId;
		this.databaseTable = Configuration.getInstance().getDatabaseMetadata().getTable(tableName);
		this.pk = databaseTable.getPrimaryKey();
		this.modified = false;

		this.writeSet = new TableWriteSet(this.databaseTable.getName());

		this.fromItemTemp = new Table(Configuration.getInstance().getDatabaseName(), this.databaseTable.getName());
		this.selectAllItems = this.databaseTable.getFieldsNamesList();

		this.duplicatedRows = new HashSet<>();
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
						LOG.debug("I identified one primary key from your where clause " + pk);
						if(subExpressionStrs[i].contains("="))
						{
							String tempStr = subExpressionStrs[i].replaceAll("\\s+", "");
							LOG.debug("I remove all space " + tempStr);
							int indexOfEqualSign = tempStr.indexOf('=');
							if(indexOfEqualSign < tempStr.length() - 1)
							{
								String valuePart = tempStr.substring(indexOfEqualSign + 1);
								if(this.isInteger(valuePart))
								{
									LOG.debug("We identified an integer");
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
		// ScratchpadDefaults.SCRATCHPAD_COL_VV;
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
		buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_VV);
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
	public void setup(DatabaseMetaData metadata, IDBScratchpad scratchpad)
	{
		try
		{
			this.tempTableName = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.databaseTable.getName() +
					"_" +
					scratchpad.getScratchpadId();
			this.tempTableNameAlias = ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + this.tableId;
			String tableNameAlias = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.tableId;

			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("DROP TABLE IF EXISTS ");
			StringBuffer buffer = new StringBuffer();

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append("CREATE LOCAL TEMPORARY TABLE ");    // for H2
			else
				buffer.append("CREATE TABLE IF NOT EXISTS ");        // for mysql

			LOG.debug("creating temporary table {}", this.tempTableName);

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
				LOG.trace("UNIQUE INDEX" + columnName);
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

			scratchpad.executeUpdate(buffer2.toString());
			scratchpad.executeUpdate(buffer.toString());

		} catch(SQLException e)
		{
			LOG.error("failed to create temporary tables for scratchpad {}", scratchpad.getScratchpadId());
			RuntimeHelper.throwRunTimeException("scratchpad creation failed", ExitCode.SCRATCHPAD_INIT_FAILED);
		}
		LOG.info("executor for table {} created", this.databaseTable.getName());
	}

	/**
	 * Execute select operation in the temporary table
	 *
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param tables
	 *
	 * @return
	 *
	 * @throws SQLException
	 * @throws ScratchpadException
	 */
	public Result executeTempOpSelect(DBSingleOperation op, Select dbOp, IDBScratchpad db, IExecuter[] policies,
									  String[][] tables) throws SQLException, ScratchpadException
	{
		Debug.println("multi table select >>" + dbOp);
		HashMap<String, Integer> columnNamesToNumbersMap = new HashMap<>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("select ");                        // select in base table
		PlainSelect select = (PlainSelect) dbOp.getSelectBody();
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
				if(str.startsWith("COUNT(") || str.startsWith("count(") || str.startsWith("MAX(") || str.startsWith(
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
				policies[i].addFromTablePlusPrimaryKeyValues(buffer, !db.isReadOnly(), tables[i], whereConditionStr);
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
		//addGroupBy(buffer, select.getGroupByColumnReferences(), policies, tables, false);
		//addOrderBy(buffer, select.getOrderByElements(), policies, tables, false);
		//addLimit(buffer, select.getLimit());
		buffer.append(";");

		//Debug.println( "---->" + buffer.toString());
		//Debug.println( "---->" + columnNamesToNumbersMap.toString().toString());
		//System.err.println( "---->" + buffer.toString());
		List<String[]> result = new ArrayList<>();
		ResultSet rs = db.executeQuery(buffer.toString());
		//addToResultList(rs, result, db, policies, !aggregateQuery);
		rs.close();
		return DBSelectResult.createResult(result, columnNamesToNumbersMap);

	}

	@Override
	public ResultSet executeTemporaryQueryOnSingleTable(DBSingleOperation dbOp, IDBScratchpad db)
			throws SQLException, ScratchpadException
	{
		if(dbOp.getStatement() instanceof Select)
			return this.executeTempOpSelect((Select) dbOp.getStatement(), db);
		else
			RuntimeHelper.throwRunTimeException("query operation expected", ExitCode.UNEXPECTED_OP);
		return null;
	}

	@Override
	public ResultSet executeTemporaryQueryOnMultTable(DBSingleOperation dbOp, IDBScratchpad db, IExecuter[] policies,
													  String[][] tables) throws SQLException, ScratchpadException
	{
		if(dbOp.getStatement() instanceof Select)
			return executeTempOpSelect((Select) dbOp.getStatement(), db, policies, tables);
		else
			RuntimeHelper.throwRunTimeException("query operation expected", ExitCode.UNEXPECTED_OP);
		return null;
	}

	@Override
	public Result executeTemporaryUpdate(DBSingleOperation dbOp, IDBScratchpad db)
			throws SQLException, ScratchpadException
	{
		modified = true;
		if(dbOp.getStatement() instanceof Insert)
			return executeTempOpInsert(dbOp, (Insert) dbOp.getStatement(), db);
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
	public void resetExecuter(IDBScratchpad pad) throws SQLException
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("TRUNCATE TABLE ");
		buffer.append(this.tempTableName);
		pad.addToBatchUpdate(buffer.toString());
		this.writeSet.reset();
		this.duplicatedRows.clear();
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
	protected DBUpdateResult executeTempOpInsert(DBSingleOperation op, Insert insertOp, IDBScratchpad db)
			throws SQLException
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("insert into ");
		buffer.append(tempTableName);

		List columnsList = insertOp.getColumns();
		List valuesList = ((ExpressionList) insertOp.getItemsList()).getExpressions();

		Integer immutablueValue = null;

		if(columnsList == null)
		{
			buffer.append("(");
			buffer.append(tableDefinition.getPlainColumnList());
			buffer.append(")");
		} else
		{
			buffer.append("(");
			Iterator it = columnsList.iterator();
			boolean first = true;
			while(it.hasNext())
			{
				String col = it.next().toString();

				if(!first)
					buffer.append(",");
				first = false;
				buffer.append(col);
			}
			buffer.append(")");
		}

		int counter = 0;
		List<String> pkValueList = new ArrayList<>();

		for(int i = 0; i < pk.getSize(); i++)
		{
			pkValueList.add(valuesList.get(i).toString());
			counter++;
		}

		PrimaryKeyValue pkValue = new PrimaryKeyValue(pkValueList, this.databaseTable.getName());
		this.createWriteSetEntry(pkValue);
		this.writeSet.addInsertedRow(pkValue);

		for(int j = counter; j < valuesList.size(); j++)
		{
			String valueString = valuesList.toString();
			this.addNewEntry(pkValue, columnsList.get(counter).toString(), valueString);
			counter++;
		}

		buffer.append(" values ");
		buffer.append(insertOp.getItemsList());
		buffer.append(";");

		// TODO: blue transactions need to fail here when the value inserted already exists
		int result = db.executeUpdate(buffer.toString());
		String[] pkVal = tableDefinition.getPlainPKValue(insertOp.getColumns(), insertOp.getItemsList());
		if(pkVal.length > 0)
		{
			db.addToWriteSet(DBWriteSetEntry.createEntry(insertOp.getTable().toString(), pkVal, true, false));
		}

		//add unique index to write set as well
		//get unique indices
		String[] uniqueIndicesValue = tableDefinition.getPlainUniqueIndexValue(insertOp.getColumns(),
				insertOp.getItemsList());
		for(int i = 0; i < uniqueIndicesValue.length; i++)
		{
			String[] uiqStr = new String[1];
			uiqStr[0] = uniqueIndicesValue[i];
			db.addToWriteSet(DBWriteSetEntry.createEntry(insertOp.getTable().toString(), uiqStr, true, false));
		}

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
	private DBUpdateResult executeTempOpDelete(Delete deleteOp, IDBScratchpad db) throws SQLException
	{
		LOG.trace("fetching rows to delete");
		StringBuffer buffer = new StringBuffer();
		buffer.append("(SELECT ");
		buffer.append(this.pk.getQueryClause());
		buffer.append(" FROM ");
		buffer.append(deleteOp.getTable().toString());
		addWhere(buffer, deleteOp.getWhere());
		buffer.append(" AND ");
		this.addNotInClause(buffer, true, true);
		buffer.append(") UNION (SELECT ");
		buffer.append(this.pk.getQueryClause());
		buffer.append(" FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, deleteOp.getWhere());
		buffer.append(")");
		buffer.append(";");
		String query = buffer.toString();

		// this should only return 1 row...
		ResultSet res = db.executeQuery(query);
		int rowsDeleted = 0;

		while(res.next())
		{
			rowsDeleted++;
			String pkValueString = this.getPkValue(res);
			PrimaryKeyValue pkValue = new PrimaryKeyValue(pkValueString, this.databaseTable.getName());

			/* we will remove this row.
			thus, remove it form duplicatedRows and add it to deletedRows */
			if(this.writeSet.getInsertedRows().contains(pkValue))
				this.writeSet.removeInsertedRow(pkValue);
			else
				this.writeSet.removeUpdatedRow(pkValue);

			this.writeSet.addDeletedRow(pkValue);

			int nPks = tableDefinition.getPksPlain().length;
			if(nPks > 0)
			{
				String[] pkVal = new String[nPks];
				for(int i = 0; i < pkVal.length; i++)
					pkVal[i] = res.getObject(i + 1).toString();
				db.addToWriteSet(DBWriteSetEntry.createEntry(deleteOp.getTable().toString(), pkVal, true, true));
			}

			String[] uniqueIndexStrs = tableDefinition.getUqIndicesPlain();
			for(int k = 0; k < uniqueIndexStrs.length; k++)
			{
				String[] uqStr = new String[1];
				uqStr[0] = res.getString(uniqueIndexStrs[k]);
				db.addToWriteSet(DBWriteSetEntry.createEntry(tableDefinition.name, uqStr, true, true));
			}
		}

		res.close();
		buffer = new StringBuffer();
		buffer.append("delete from ");
		buffer.append(this.tempTableName);
		buffer.append(" where ");
		buffer.append(deleteOp.getWhere().toString());
		buffer.append(";");
		String delete = buffer.toString();

		LOG.trace("erasing {} rows from temporary table", rowsDeleted);

		rowsDeleted += db.executeUpdate(delete);
		return DBUpdateResult.createResult(rowsDeleted);
	}

	private DBUpdateResult executeTempOpUpdate(Update updateOp, IDBScratchpad db) throws SQLException
	{
		LOG.trace("insert missing rows in temporary table before applying update");
		// before writting in the scratchpad, add the missing rows
		// we also use this method to capture all the tuples that will be affected by the update
		this.addMissingRowsToScratchpad(updateOp, db);

		List<String> affectedRows = this.getResultSelectBeforeUpdate(updateOp, db);

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
			DataField field = this.databaseTable.getField(columnName);

			if(field.isImmutableField())
				RuntimeHelper.throwRunTimeException("trying to modify an immutable field", ExitCode.UNEXPECTED_OP);

			// we are updating this field, lets check if it is a valid value
			this.verifyCheckConstraints(field, newValue);

			for(String pkValue : affectedRows)
				this.addNewEntry(new PrimaryKeyValue(pkValue, this.databaseTable.getName()), columnName, newValue);

			buffer.append(columnName);
			buffer.append(" = ");
			buffer.append(newValue);
			if(colIt.hasNext())
				buffer.append(" , ");
		}
		buffer.append(" WHERE ");
		buffer.append(updateOp.getWhere().toString());

		db.addToBatchUpdate(buffer.toString());
		db.executeBatch();
		this.modified = true;

		return DBUpdateResult.createResult(affectedRows.size());
	}

	private ResultSet executeTempOpSelect(Select selectOp, IDBScratchpad db) throws SQLException, ScratchpadException
	{
		LOG.trace("creating selection for query {}", selectOp.toString());
		String queryToOrigin;
		String queryToTemp;

		StringBuffer buffer = new StringBuffer();

		PlainSelect plainSelect = (PlainSelect) selectOp.getSelectBody();
		List columnsToFetch = plainSelect.getSelectItems();

		if(columnsToFetch.size() == 1 && columnsToFetch.get(0).toString().equalsIgnoreCase("*"))
			plainSelect.setSelectItems(this.selectAllItems);

		StringBuffer whereClauseTemp = new StringBuffer();

		if(plainSelect.getWhere() != null)
		{
			whereClauseTemp.append("(");
			whereClauseTemp.append(plainSelect.getWhere());
			whereClauseTemp.append(") AND");
		}

		whereClauseTemp.append(" (");
		whereClauseTemp.append(ScratchpadDefaults.SCRATCHPAD_COL_DELETED);
		whereClauseTemp.append(" = FALSE )");
		StringBuffer whereClauseOrig = new StringBuffer(whereClauseTemp);
		if(this.duplicatedRows.size() > 0 || this.writeSet.getDeletedRows().size() > 0)
		{
			whereClauseOrig.append(" AND ");
			this.addNotInClause(whereClauseOrig, true, true);
		}

		String defaultWhere = plainSelect.getWhere().toString();
		queryToOrigin = plainSelect.toString();

		plainSelect.setFromItem(this.fromItemTemp);
		queryToTemp = plainSelect.toString();

		queryToOrigin = StringUtils.replace(queryToOrigin, defaultWhere, whereClauseOrig.toString());
		queryToTemp = StringUtils.replace(queryToTemp, defaultWhere, whereClauseTemp.toString());

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

	private ResultSet executeTempOpSelect(Select selectOp, IDBScratchpad db, IExecuter[] executors, String[][] tables)
			throws SQLException, ScratchpadException
	{
		return db.executeQuery(selectOp.toString());
	/*
		LOG.trace("creating selection for query {}", selectOp.toString());
		String queryToOrigin;
		String queryToTemp;

		StringBuffer buffer = new StringBuffer();

		PlainSelect plainSelect = (PlainSelect) selectOp.getSelectBody();
		List columnsToFetch = plainSelect.getSelectItems();

		if(columnsToFetch.size() == 1 && columnsToFetch.get(0).toString().equalsIgnoreCase("*"))
			plainSelect.setSelectItems(this.selectAllItems);

		StringBuffer whereClauseTemp = new StringBuffer();

		if(plainSelect.getWhere() != null)
		{
			whereClauseTemp.append("(");
			whereClauseTemp.append(plainSelect.getWhere());
			whereClauseTemp.append(") AND");
		}

		whereClauseTemp.append(" (");
		whereClauseTemp.append(ScratchpadDefaults.SCRATCHPAD_COL_DELETED);
		whereClauseTemp.append(" = FALSE )");
		StringBuffer whereClauseOrig = new StringBuffer(whereClauseTemp);
		if(this.duplicatedRows.size() > 0 || this.writeSet.getDeletedRows().size() > 0)
		{
			whereClauseOrig.append(" AND ");
			this.addNotInClause(whereClauseOrig, true, true);
		}

		String defaultWhere = plainSelect.getWhere().toString();
		queryToOrigin = plainSelect.toString();

		plainSelect.setFromItem(this.fromItemTemp);
		queryToTemp = plainSelect.toString();

		queryToOrigin = StringUtils.replace(queryToOrigin, defaultWhere, whereClauseOrig.toString());
		queryToTemp = StringUtils.replace(queryToTemp, defaultWhere, whereClauseTemp.toString());

		buffer.append("(");
		buffer.append(queryToTemp);
		buffer.append(")");
		buffer.append(" UNION ");
		buffer.append("(");
		buffer.append(queryToOrigin);
		buffer.append(")");

		String finalQuery = buffer.toString();
		LOG.trace("query generated: {}", finalQuery);

		return db.executeQuery(finalQuery);  */
	}

	/**
	 * Fills the buffer with the duplicated values
	 *
	 * @param buffer
	 * @param startWithComa
	 */
	private void fillDuplicatedValues(StringBuffer buffer, boolean startWithComa)
	{
		if(startWithComa)
			buffer.append(",");

		boolean first = true;
		for(PrimaryKeyValue pkValue : this.duplicatedRows)
		{
			if(first)
			{
				buffer.append(pkValue.getValue());
				first = false;
			} else
			{
				buffer.append(",");
				buffer.append(pkValue.getValue());
			}
		}
	}

	/**
	 * Fills the buffer with the duplicated values
	 *
	 * @param buffer
	 * @param startWithComa
	 */
	private void fillDeletedValues(StringBuffer buffer, boolean startWithComa)
	{
		if(startWithComa)
			buffer.append(",");

		boolean first = true;
		for(PrimaryKeyValue pkValue : this.writeSet.getDeletedRows())
		{
			if(first)
			{
				buffer.append(pkValue.getValue());
				first = false;
			} else
			{
				buffer.append(",");
				buffer.append(pkValue.getValue());
			}
		}
	}

	/**
	 * Fills the buffer with the newly inserted values
	 *
	 * @param buffer
	 * @param startWithComa
	 */
	private void fillInsertedValues(StringBuffer buffer, boolean startWithComa)
	{
		if(startWithComa)
			buffer.append(",");

		boolean first = true;
		for(PrimaryKeyValue pkValue : this.writeSet.getInsertedRows())
		{
			if(first)
			{
				buffer.append(pkValue.getValue());
				first = false;
			} else
			{
				buffer.append(",");
				buffer.append(pkValue.getValue());
			}
		}
	}

	/**
	 * Inserts missing rows in the temporary table.
	 * This must be done before updating rows in the scratchpad.
	 * This allows the update operation to be executed only in the scratchpad while affecting the intended rows.
	 *
	 * @param updateOp
	 * @param pad
	 *
	 * @throws SQLException
	 */
	private void addMissingRowsToScratchpad(Update updateOp, IDBScratchpad pad) throws SQLException
	{
		List<Integer> affectedRows = new ArrayList<>();
		int affected = 0;

		StringBuffer buffer = new StringBuffer();
		buffer.append("(SELECT *, '" + updateOp.getTable().toString() + "' as tname FROM ");
		buffer.append(updateOp.getTable().toString());
		addWhere(buffer, updateOp.getWhere());
		buffer.append(") UNION (select *, '" + this.tempTableName + "' as tname FROM ");
		buffer.append(this.tempTableName);
		addWhere(buffer, updateOp.getWhere());
		buffer.append(");");

		ResultSet res = pad.executeQuery(buffer.toString());
		while(res.next())
		{
			// since we must iterate all the result set to check which tuples must be added to the temp table,
			// we can capture here what tuples will be affected and track theirs rowIds

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
				//affectedRows.add(res.getInt(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE));
				continue;
			}

			String pkValueString = this.getPkValue(res);
			PrimaryKeyValue pkValue = new PrimaryKeyValue(pkValueString, this.databaseTable.getName());

			this.createWriteSetEntry(pkValue);
			this.duplicatedRows.add(pkValue);
			this.writeSet.addUpdatedRow(pkValue);

			affected++;

			buffer.setLength(0);
			buffer.append("insert into ");
			buffer.append(this.tempTableName);
			buffer.append(" values (");

			for(int i = 0; i < this.tableDefinition.colsPlain.length; i++)
			{
				Object oldValue = res.getObject(i + 1);
				String oldValueString;

				if(oldValue == null)
					oldValueString = "NULL";
				else
					oldValueString = oldValue.toString();

				if(this.tableDefinition.colsStr[i])
					oldValueString = "\"" + oldValueString + "\"";

				if(i > 0)
					buffer.append(",");

				buffer.append(oldValueString);
				this.addOldEntry(pkValue, this.tableDefinition.colsPlain[i], oldValueString);
			}
			buffer.append(");");
			pad.executeUpdate(buffer.toString());
		}
		LOG.debug("{} affected by this update", affected);
		res.close();
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

	/**
	 * Adds the 'NOT IN' clause to the buffer.
	 * Does not add the prefix 'AND' to the buffer
	 *
	 * @param buffer
	 * @param filterDeleted
	 * @param filterDuplicated
	 */
	private void addNotInClause(StringBuffer buffer, boolean filterDeleted, boolean filterDuplicated)
	{
		boolean notInClauseAdded = false;
		boolean needComma = false;

		if(this.duplicatedRows.size() > 0 && filterDuplicated)
		{
			buffer.append("((");
			buffer.append(this.pk.getQueryClause());
			buffer.append(")");
			buffer.append(" NOT IN (");
			notInClauseAdded = true;
			fillDuplicatedValues(buffer, needComma);
			needComma = true;
		}

		if(this.writeSet.getDeletedRows().size() > 0 && filterDeleted)
		{
			if(!notInClauseAdded)
			{
				buffer.append("AND ((");
				buffer.append(this.pk.getQueryClause());
				buffer.append(") NOT IN (");
				notInClauseAdded = true;
			}

			fillDeletedValues(buffer, needComma);
		}

		if(notInClauseAdded)
			buffer.append("))");
	}

	@Override
	public TableWriteSet getWriteSet() throws SQLException
	{
		//FIXME: whaaat?
		//this.writeSet.setWriteSet(this.tuplesWriteSet);
		//ResultSet updateResultSet = this.getUpdateResultSet(pad);
		//ResultSet insertResultSet = this.getInserteResultSet(pad);

		//this.writeSet.setInsertResultSet(insertResultSet);
		//this.writeSet.setUpdateResultSet(updateResultSet);

		return this.writeSet;
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

	/**
	 * Returns a list of _SP_immut values that matches
	 * the tuples that will be affected by the update operation
	 *
	 * @param updateOp
	 * @param pad
	 *
	 * @return
	 *
	 * @throws SQLException
	 */
	private List<String> getResultSelectBeforeUpdate(Update updateOp, IDBScratchpad pad) throws SQLException
	{
		Expression whereClause = updateOp.getWhere();
		if(whereClause == null)
			RuntimeHelper.throwRunTimeException("operation with no where clause not yey supported",
					ExitCode.MISSING_IMPLEMENTATION);

		// just selec the _SP_immut field.
		// its all we need to keep track of changes
		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(this.databaseTable.getPrimaryKey().getQueryClause());
		buffer.append(" from ");
		buffer.append(this.tempTableName);
		buffer.append(" where ");
		buffer.append(whereClause);

		ResultSet rs = pad.executeQuery(buffer.toString());

		List<String> pksList = new LinkedList<>();

		while(rs.next())
		{
			String pkValue = this.getPkValue(rs);
			pksList.add(pkValue);
		}

		return pksList;
	}

	/**
	 * Creates an write set entry for a tuple
	 *
	 * @param rowId
	 */
	private void createWriteSetEntry(PrimaryKeyValue pkValue)
	{
		TupleWriteSet set = new TupleWriteSet(pkValue);
		this.writeSet.getTableWriteSetMap().put(pkValue, set);
	}

	private void addNewEntry(PrimaryKeyValue pkValue, String fieldName, String newValue)
	{
		this.writeSet.getTableWriteSetMap().get(pkValue).addLwwEntry(fieldName, newValue);
	}

	private void addOldEntry(PrimaryKeyValue pkValue, String fieldName, String oldValue)
	{
		this.writeSet.getTableWriteSetMap().get(pkValue).addOldEntry(fieldName, oldValue);
	}

	private String getPkValue(ResultSet rs) throws SQLException
	{
		StringBuilder pkBuffer = new StringBuilder();

		for(int i = 0; i < this.pk.getSize(); i++)
		{
			String fieldValue = rs.getObject(i + 1).toString();

			if(this.tableDefinition.colsStr[i])
				fieldValue = "\"" + fieldValue + "\"";

			if(i == 0)
				pkBuffer.append(fieldValue);
			else
			{
				pkBuffer.append(",");
				pkBuffer.append(fieldValue);
			}
		}
		return pkBuffer.toString();
	}

}