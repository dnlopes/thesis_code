/**
 *
 */

package runtime;


import crdtlib.CrdtFactory;
import database.jdbc.ConnectionFactory;
import database.util.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import network.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IDFactories.IdentifierFactory;
import util.ExitCode;
import util.debug.Debug;
import util.defaults.Configuration;
import util.defaults.ScratchpadDefaults;
import util.parser.DDLParser;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;


/**
 * Created by dnlopes on 13/03/15.
 */
public class MyShadowOpCreator
{

	static final Logger LOG = LoggerFactory.getLogger(MyShadowOpCreator.class);

	static HashMap<String, DatabaseTable> annotatedTableSchema;
	private DatabaseMetadata databaseMetadata;

	static CCJSqlParserManager cJsqlParser;

	static boolean isInitialized = false;

	/** The defaultConnection for fetching subselection data. */
	Connection defaultConnection;

	/** The date format instance, it is thread-local since it is not thread-safe. */
	DateFormat dateFormat;

	/**
	 * Instantiates a new crdt transformer.
	 *
	 * @param schemaFilePath
	 * 		the sql schema
	 * @param propertiesStr
	 * 		the properties str
	 * @param userName
	 * 		the user name
	 * @param password
	 * 		the password
	 * @param gPId
	 * 		the g p id
	 * @param numOfProxies
	 * 		the num of proxies
	 * @param txMudConn
	 * 		the tx mud conn
	 */
	public MyShadowOpCreator(String schemaFilePath, Proxy proxy) throws SQLException
	{
		if(!isInitialized)
		{
			Connection originalConn = ConnectionFactory.getDefaultConnection(proxy.getConfig());
			DDLParser sP = new DDLParser(schemaFilePath);
			databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
			sP.parseAnnotations();
			annotatedTableSchema = sP.getTableCrdtFormMap();
			cJsqlParser = new CCJSqlParserManager();
			//iDFactory = new IDFactories();
			//this.initIDFactories(globalProxyId, totalProxyNum, originalConn);
			isInitialized = true;
			this.closeRealConnection(originalConn);
		}
		this.defaultConnection = ConnectionFactory.getDefaultConnection(Configuration.getInstance().getDatabaseName());
		this.setDateFormat(DatabaseFunction.getNewDateFormatInstance());
	}

	/**
	 * Close real connection.
	 *
	 * @param originalConn
	 * 		the original conn
	 */
	private void closeRealConnection(Connection originalConn)
	{
		LOG.trace("We have initialized the id factory, now close the real connection");
		try
		{
			originalConn.close();
		} catch(SQLException e)
		{
			LOG.warn("failed to close original connection");
		}
	}

	/*
	 * The following two functions are used to fill in missing fields in an
	 * insertion.
	 */

	/**
	 * Find missing data fields.
	 *
	 * @param tableName
	 * 		the table name
	 * @param colList
	 * 		the col list
	 * @param valueList
	 * 		the value list
	 *
	 * @return the sets the
	 */
	public Set<String> findMissingDataFields(String tableName, List<String> colList, List<String> valueList)
	{
		//DatabaseTable dtB = annotatedTableSchema.get(tableName);
		DatabaseTable dtB = this.databaseMetadata.getTable(tableName);
		return dtB.findMisingDataField(colList, valueList);
	}

	/**
	 * Fill in missing value.
	 *
	 * @param tableName
	 * 		the table name
	 * @param colList
	 * 		the col list
	 * @param valueList
	 * 		the value list
	 */
	public void fillInMissingValue(String tableName, List<String> colList, List<String> valueList)
	{

		Set<String> missFields = findMissingDataFields(tableName, colList, valueList);

//		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);

		for(int i = 0; i < valueList.size(); i++)
		{
			DataField dF = null;
			if(colList != null && colList.size() > 0)
			{
				dF = dbT.getField(colList.get(i));
			} else
			{
				dF = dbT.getField(i);
			}
			String expStr = valueList.get(i).trim();
			if(expStr.equalsIgnoreCase("NOW()") || expStr.equalsIgnoreCase("NOW") || expStr.equalsIgnoreCase(
					"CURRENT_TIMESTAMP") || expStr.equalsIgnoreCase("CURRENT_TIMESTAMP()") || expStr.equalsIgnoreCase(
					"CURRENT_DATE"))
			{
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
			}
		}

		// fill in the missing tuples
		if(missFields != null)
		{
			for(String missingDfName : missFields)
			{
				colList.add(missingDfName);
				DataField dF = dbT.getField(missingDfName);
				if(dF.isPrimaryKey())
				{
					if(dF.isForeignKey())
					{
						try
						{
							throw new RuntimeException("Foreign primary key must be specified " + missingDfName + "!");
						} catch(RuntimeException e)
						{
							e.printStackTrace();
							System.exit(ExitCode.FOREIGNPRIMARYKEYMISSING);
						}
					} else
					{
						/*valueList.add(Integer.toString(iDFactory.getNextId(
								tableName, dF.getFieldName())));*/
						throw new RuntimeException("The primary keys' values should not be missing");
					}
				} else
				{
					if(dF.isAutoIncrement())
					{
						int nextId = IdentifierFactory.getNextId(dF);
						valueList.add(String.valueOf(nextId));
					}
					else if(dF.getDefaultValue() == null)
					{
						valueList.add(CrdtFactory.getDefaultValueForDataField(this.getDateFormat(), dF));
					} else
					{
						if(dF.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP"))
						{
							valueList.add("'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
						} else
						{
							valueList.add(dF.getDefaultValue());
						}
					}
				}
			}
		}
	}

	/*
	 * The following function is used to replace NOW or CURRENT_TIMESTAMP
	 * functions in an update
	 */

	/**
	 * Replace value for database functions.
	 *
	 * @param tableName
	 * 		the table name
	 * @param valueList
	 * 		the value list
	 */
	public void replaceValueForDatabaseFunctions(String tableName, List<String> valueList)
	{
		for(int i = 0; i < valueList.size(); i++)
		{
			String valStr = valueList.get(i).trim();
			if(valStr.equalsIgnoreCase("NOW()") || valStr.equalsIgnoreCase("NOW") || valStr.equalsIgnoreCase(
					"CURRENT_TIMESTAMP") || valStr.equalsIgnoreCase("CURRENT_TIMESTAMP()") || valStr.equalsIgnoreCase(
					"CURRENT_DATE"))
			{
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
			}
		}
	}

	/**
	 * Checks if is primary key missing from where clause.
	 *
	 * @param tableName
	 * 		the table name
	 * @param whereClause
	 * 		the where clause
	 *
	 * @return true, if is primary key missing from where clause
	 */
	private boolean isPrimaryKeyMissingFromWhereClause(String tableName, Expression whereClause)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);
		return dbT.isPrimaryKeyMissingFromWhereClause(whereClause.toString());
	}

	/*
	 * This function is to check whether the delete and update are specified by
	 * a or a group of primary keys. If not, it will generate a query to fetch
	 * the list of records matching the condition If the return value is not
	 * empty, then please executeUpdate your return string to fetch primary keys;
	 * Otherwise please ignore the function
	 */

	/**
	 * Gets the primary key selection query.
	 *
	 * @param tableName
	 * 		the table name
	 * @param whereClause
	 * 		the where clause
	 *
	 * @return the primary key selection query
	 */
	public String getPrimaryKeySelectionQuery(String tableName, Expression whereClause)
	{
//		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);

		return dbT.generatedPrimaryKeyQuery(whereClause.toString());
	}

	// intercept the executeUpdate,make it to deterministic

	/**
	 * Make to deterministic.
	 *
	 * @param sqlQuery
	 * 		the sql query
	 *
	 * @return the string[]
	 *
	 * @throws net.sf.jsqlparser.JSQLParserException
	 * 		the jSQL parser exception
	 */
	public String[] makeToDeterministic(String sqlQuery) throws JSQLParserException
	{
		String[] deterQueries = null;

		// contains current_time_stamp
		// contains NOW(), use the same
		// contains select, do the select first
		// contains delete from where (not specify by full primary key)
		// fill in default value and IDs for insert
		net.sf.jsqlparser.statement.Statement sqlStmt = cJsqlParser.parse(new StringReader(sqlQuery));
		if(sqlStmt instanceof Insert)
		{
			Insert insertStmt = (Insert) sqlStmt;
			String tableName = insertStmt.getTable().getName();
			List<String> colList = new ArrayList<>();
			List<String> valList = new ArrayList<>();
			Iterator colIt = insertStmt.getColumns().iterator();
			while(colIt.hasNext())
			{
				colList.add(colIt.next().toString());
			}
			//replace selection with their results
			replaceSelectionForInsert(insertStmt, valList);
			//call function to replace and fill in the missing fields
			fillInMissingValue(tableName, colList, valList);
			this.addScratchpadColumnsValues(tableName, colList, valList);

			deterQueries = new String[1];
			deterQueries[0] = assembleInsert(tableName, colList, valList);
		} else if(sqlStmt instanceof Update)
		{
			Update updateStmt = (Update) sqlStmt;
			List<String> colList = new ArrayList<>();
			List<String> valList = new ArrayList<>();
			Iterator colIt = updateStmt.getColumns().iterator();
			while(colIt.hasNext())
			{
				colList.add(colIt.next().toString());
			}
			Iterator valueIt = updateStmt.getExpressions().iterator();
			while(valueIt.hasNext())
			{
				valList.add(valueIt.next().toString());
			}
			//replace values for selection in the itemlist
			replaceSelectionForUpdate(updateStmt, valList);
			//replace database functions like now or current time stamp
			replaceValueForDatabaseFunctions(updateStmt.getTable().getName(), valList);
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query
			deterQueries = fillInMissingPrimaryKeysForUpdate(updateStmt, colList, valList);

		} else if(sqlStmt instanceof Delete)
		{
			Delete deleteStmt = (Delete) sqlStmt;
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query
			deterQueries = fillInMissingPrimaryKeysForDelete(deleteStmt);
		}

		return deterQueries;
	}

	/*
	 * These two following functions are to fetch primary key sets for an update/delete which doesn't
	 * specify the full primary keys in its where clause.
	 * Example: update t1 set a = f where condition, condition doesn't contain all
	 * primary keys. The problem with this update is that it will introduce different
	 * changes to system if they apply against different state.
	 */

	/**
	 * Fill in missing primary keys for update.
	 *
	 * @param updateStmt
	 * 		the update stmt
	 * @param colList
	 * 		the col list
	 * @param valList
	 * 		the val list
	 *
	 * @return the string[]
	 */
	public String[] fillInMissingPrimaryKeysForUpdate(Update updateStmt, List<String> colList, List<String> valList)
	{
		String[] newUpdates = null;

		if(this.isPrimaryKeyMissingFromWhereClause(updateStmt.getTable().getName(), updateStmt.getWhere()))
		{
			String primaryKeySelectStr = getPrimaryKeySelectionQuery(updateStmt.getTable().getName(),
					updateStmt.getWhere());
			//executeUpdate the primaryKeySelectStr
			try
			{
				LOG.trace("fetching rows from main database");
				PreparedStatement sPst = defaultConnection.prepareStatement(primaryKeySelectStr);
				ResultSet rs = sPst.executeQuery();
				newUpdates = assembleUpdates(updateStmt.getTable().getName(), colList, valList, rs);
				rs.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
				Debug.println("Selection is wrong");
			}
		} else
		{
			//Debug.println("No primary key missing, then return the original update query");
			newUpdates = new String[1];
			newUpdates[0] = assembleUpdate(updateStmt.getTable().getName(), colList, valList,
					updateStmt.getWhere().toString());
		}
		return newUpdates;
	}

	/**
	 * Fill in missing primary keys for delete.
	 *
	 * @param delStmt
	 * 		the del stmt
	 *
	 * @return the string[]
	 */
	public String[] fillInMissingPrimaryKeysForDelete(Delete delStmt)
	{
		String[] newDeletes = null;
		if(this.isPrimaryKeyMissingFromWhereClause(delStmt.getTable().getName(), delStmt.getWhere()))
		{
			String primaryKeySelectStr = this.getPrimaryKeySelectionQuery(delStmt.getTable().getName(),
					delStmt.getWhere());
			//executeUpdate the primaryKeySelectStr
			try
			{
				PreparedStatement sPst = defaultConnection.prepareStatement(primaryKeySelectStr);
				ResultSet rs = sPst.executeQuery();
				newDeletes = assembleDeletes(delStmt.getTable().getName(), rs);
				rs.close();
			} catch(SQLException e)
			{
				e.printStackTrace();
				Debug.println("Selection is wrong");
			}
		} else
		{
			//Debug.println("No primary key missing, then return the original delete query");
			newDeletes = new String[1];
			newDeletes[0] = delStmt.toString();
		}
		return newDeletes;
	}

	/*
	 * The following two functions are used to replace the values for selection, encode their results
	 */

	/**
	 * Replace selection for insert.
	 *
	 * @param insertStmt
	 * 		the insert stmt
	 * @param valList
	 * 		the val list
	 *
	 * @throws JSQLParserException
	 * 		the jSQL parser exception
	 */
	public void replaceSelectionForInsert(Insert insertStmt, List<String> valList) throws JSQLParserException
	{
		Iterator valueIt = ((ExpressionList) insertStmt.getItemsList()).getExpressions().iterator();
		while(valueIt.hasNext())
		{
			String valStr = valueIt.next().toString().trim();
			if(valStr.contains("SELECT") || valStr.contains("select"))
			{
				//executeUpdate the selection
				//remove two brackets
				if(valStr.indexOf("(") == 0 && valStr.lastIndexOf(")") == valStr.length() - 1)
				{
					valStr = valStr.substring(1, valStr.length() - 1);
				}
				PlainSelect plainSelect = ((PlainSelect) ((Select) cJsqlParser.parse(
						new StringReader(valStr))).getSelectBody());
				int selectItemCount = plainSelect.getSelectItems().size();
				PreparedStatement sPst;
				try
				{
					sPst = defaultConnection.prepareStatement(valStr);
					ResultSet rs = sPst.executeQuery();
					if(rs.next())
					{
						for(int i = 0; i < selectItemCount; i++)
						{
							//Debug.println("we got something from the subselection : " + rs.getString(i+1));
							valList.add(rs.getString(i + 1));
						}
					} else
					{
						throw new RuntimeException("Select must return a value!");
					}
					rs.close();
				} catch(SQLException e1)
				{
					e1.printStackTrace();
				}
			} else
			{
				valList.add(valStr);
			}
		}
	}

	/**
	 * Replace selection for update.
	 *
	 * @param upStmt
	 * 		the up stmt
	 * @param valList
	 * 		the val list
	 *
	 * @throws JSQLParserException
	 * 		the jSQL parser exception
	 */
	public void replaceSelectionForUpdate(Update upStmt, List<String> valList) throws JSQLParserException
	{
		Iterator valueIt = upStmt.getExpressions().iterator();
		int colIndex = 0;
		while(valueIt.hasNext())
		{
			String valStr = valueIt.next().toString().trim();
			if(valStr.contains("SELECT") || valStr.contains("select"))
			{
				//executeUpdate the selection
				//remove two brackets
				if(valStr.indexOf("(") == 0 && valStr.lastIndexOf(")") == valStr.length() - 1)
				{
					valStr = valStr.substring(1, valStr.length() - 1);
				}
				PlainSelect plainSelect = ((PlainSelect) ((Select) cJsqlParser.parse(
						new StringReader(valStr))).getSelectBody());
				assert (plainSelect.getSelectItems().size() == 1);
				try
				{
					PreparedStatement sPst = defaultConnection.prepareStatement(valStr);
					ResultSet rs = sPst.executeQuery();
					if(rs.next())
					{
						valList.set(colIndex, rs.getObject(1).toString());
					} else
					{
						throw new RuntimeException("Select must return a value!");
					}
					rs.close();
				} catch(SQLException e)
				{
					e.printStackTrace();
					Debug.println("Selection is wrong");
				}

			}
			colIndex++;
		}
	}

	/*
	 * This function is to assemble an insertion request
	 */

	/**
	 * Assemble insert.
	 *
	 * @param tableName
	 * 		the table name
	 * @param colList
	 * 		the col list
	 * @param valList
	 * 		the val list
	 *
	 * @return the string
	 */
	public String assembleInsert(String tableName, List<String> colList, List<String> valList)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("insert into ");
		buffer.append(tableName + " ");
		if(colList.size() > 0)
		{
			buffer.append("(");
			for(int i = 0; i < colList.size(); i++)
			{
				buffer.append(colList.get(i) + ",");
			}
			buffer.setCharAt(buffer.length() - 1, ')');
		}

		buffer.append(" values (");
		if(colList.size() > 0)
		{
			for(int i = 0; i < valList.size(); i++)
			{
				buffer.append(this.get_Value_In_Correct_Format(tableName, colList.get(i), valList.get(i)) + ",");
			}
		} else
		{
			for(int i = 0; i < valList.size(); i++)
			{
				buffer.append(this.get_Value_In_Correct_Format(tableName, i, valList.get(i)) + ",");
			}
		}
		buffer.replace(buffer.length() - 1, buffer.length() + 1, ");");
		//Debug.println("Newly generated insertion is " + buffer.toString());
		return buffer.toString();
	}

	/*
	 * This function is used to assemble an update with all information
	 */

	/**
	 * Assemble update.
	 *
	 * @param tableName
	 * 		the table name
	 * @param colList
	 * 		the col list
	 * @param valList
	 * 		the val list
	 * @param whereClauseStr
	 * 		the where clause str
	 *
	 * @return the string
	 */
	public String assembleUpdate(String tableName, List<String> colList, List<String> valList, String whereClauseStr)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("update ");
		buffer.append(tableName + " set ");
		for(int i = 0; i < colList.size(); i++)
		{
			buffer.append(colList.get(i) + " = ");
			buffer.append(this.get_Value_In_Correct_Format(tableName, colList.get(i), valList.get(i)) + ",");
		}
		buffer.deleteCharAt(buffer.lastIndexOf(","));
		buffer.append(" where " + whereClauseStr + ";"); // with the ";"
		//Debug.println("Newly generated update is " + buffer.toString());
		return buffer.toString();
	}

	/*
	 * This function is used to assemble a list of updates
	 */

	/**
	 * Assemble updates.
	 *
	 * @param tableName
	 * 		the table name
	 * @param colList
	 * 		the col list
	 * @param valList
	 * 		the val list
	 * @param rs
	 * 		the rs
	 *
	 * @return the string[]
	 */
	public String[] assembleUpdates(String tableName, List<String> colList, List<String> valList, ResultSet rs)
	{
		LOG.trace("assembling update for table {}", tableName);

		StringBuilder buffer = new StringBuilder();
		String updateMainBody;
		buffer.append("update ");
		buffer.append(tableName);
		buffer.append(" set ");

		for(int i = 0; i < colList.size(); i++)
		{
			buffer.append(colList.get(i) + " = ");
			buffer.append(valList.get(i) + ",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append(" where "); // without the ";"
		updateMainBody = buffer.toString();
		//Debug.println("Newly generated update main body is " + updateMainBody);

		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);

		Set<String> pkSet = dbT.getPrimaryKeysNamesList();
		List<String> updateStrList = new ArrayList<>();
		try
		{
			while(rs.next())
			{
				StringBuilder singleUpdateStr = new StringBuilder(updateMainBody);
				int pkResultIndex = 0;
				for(String pk : pkSet)
				{
					if(pkResultIndex == 0)
					{
						singleUpdateStr.append(pk);
						singleUpdateStr.append(" = ");
						singleUpdateStr.append(rs.getString(pk));
					} else
					{
						singleUpdateStr.append(" AND ");
						singleUpdateStr.append(pk);
						singleUpdateStr.append(" = ");
						singleUpdateStr.append(rs.getString(pk));
					}
					pkResultIndex++;
				}
				//Debug.println("The newly generated update query "
				//		+ singleUpdateStr.toString());
				updateStrList.add(singleUpdateStr.toString());
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
			LOG.error("failed to assemble update query");
		}

		LOG.trace("update query assembled correctly");
		return updateStrList.toArray(new String[updateStrList.size()]);
	}

	/**
	 * Assemble delete.
	 *
	 * @param tableName
	 * 		the table name
	 * @param whereClauseStr
	 * 		the where clause str
	 *
	 * @return the string
	 */
	public String assembleDelete(String tableName, String whereClauseStr)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("delete from ");
		buffer.append(tableName + " where ");
		buffer.append(" " + whereClauseStr + ";"); // with the ";"
		//Debug.println("Newly generated delete is " + buffer.toString());
		return buffer.toString();
	}

	/**
	 * Assemble deletes.
	 *
	 * @param tableName
	 * 		the table name
	 * @param rs
	 * 		the rs
	 *
	 * @return the string[]
	 */
	public String[] assembleDeletes(String tableName, ResultSet rs)
	{
		LOG.trace("assembling deletes for table {}", tableName);

		StringBuilder buffer = new StringBuilder();
		String deleteMainBody;
		buffer.append("delete from ");
		buffer.append(tableName);
		buffer.append(" where ");
		deleteMainBody = buffer.toString();
		//Debug.println("Newly generated delete mainbody is " + deleteMainBody);

		//DatabaseTable dbTable = annotatedTableSchema.get(tableName);
		DatabaseTable dbTable = this.databaseMetadata.getTable(tableName);

		Set<String> primaryKeySet = dbTable.getPrimaryKeysNamesList();
		List<String> deleteStrList = new ArrayList<>();
		try
		{
			while(rs.next())
			{
				StringBuilder singleDeleteStrBuilder = new StringBuilder(deleteMainBody);
				int pkStrIndex = 0;
				for(String pk : primaryKeySet)
				{
					if(pkStrIndex == 0)
					{
						singleDeleteStrBuilder.append(pk);
						singleDeleteStrBuilder.append(" = ");
						singleDeleteStrBuilder.append(rs.getString(pk));
					} else
					{
						singleDeleteStrBuilder.append(" AND ");
						singleDeleteStrBuilder.append(pk);
						singleDeleteStrBuilder.append(" = ");
						singleDeleteStrBuilder.append(rs.getString(pk));
					}
					pkStrIndex++;
				}
				//Debug.println("The newly generated delete query "
				//		+ singleDeleteStrBuilder.toString());
				deleteStrList.add(singleDeleteStrBuilder.toString());
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
			LOG.error("failed to assemble delete query");
		}
		LOG.trace("delete query assembled correctly");
		return deleteStrList.toArray(new String[deleteStrList.size()]);
	}

	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param tableName
	 * 		the table name
	 * @param fieldIndex
	 * 		the field index
	 * @param Value
	 * 		the value
	 *
	 * @return the _ value_ in_ correct_ format
	 */
	public String get_Value_In_Correct_Format(String tableName, int fieldIndex, String Value)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);

		DataField dF = dbT.getField(fieldIndex);
		return dF.get_Value_In_Correct_Format(Value);
	}

	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param tableName
	 * 		the table name
	 * @param dataFileName
	 * 		the data file name
	 * @param Value
	 * 		the value
	 *
	 * @return the _ value_ in_ correct_ format
	 */
	public String get_Value_In_Correct_Format(String tableName, String dataFileName, String Value)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = this.databaseMetadata.getTable(tableName);
		DataField dF = dbT.getField(dataFileName);
		return dF.get_Value_In_Correct_Format(Value);
	}

	/**
	 * Gets the date format.
	 *
	 * @return the dateFormat
	 */
	public DateFormat getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * Sets the date format.
	 *
	 * @param dateFormat
	 * 		the dateFormat to set
	 */
	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	private void addScratchpadColumnsValues(String tableName, List<String> cols, List<String> vals)
	{
		if(cols.size() != vals.size())
		{
			LOG.error("cols and vals size must match before appending scratchpad values");
			RuntimeHelper.throwRunTimeException("cols and vals size dont match", ExitCode.ERRORTRANSFORM);
		}

		//cols.add(ScratchpadDefaults.SCRATCHPAD_COL_DELETED);
		//vals.add(ScratchpadDefaults.SCRATCHPAD_DELETED_FALSE);

		// TODO: when to add these values?
		//cols.add(ScratchpadDefaults.SCRATCHPAD_COL_TS);
		//cols.add(ScratchpadDefaults.SCRATCHPAD_COL_VV);

		cols.add(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
		int rowId = IdentifierFactory.getNextId(tableName, ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
		vals.add(Integer.toString(rowId));
	}

}

