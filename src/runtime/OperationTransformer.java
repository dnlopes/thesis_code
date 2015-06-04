package runtime;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.debug.Debug;
import util.defaults.Configuration;
import util.defaults.DBDefaults;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Created by dnlopes on 06/05/15.
 */
public class OperationTransformer
{

	private static final Logger LOG = LoggerFactory.getLogger(OperationTransformer.class);
	private static final DatabaseMetadata DB_METADATA = Configuration.getInstance().getDatabaseMetadata();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";
	private static final String SET_NOT_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=0";
	private static final String SET_DELETED_CLOCK_EXPRESION = DBDefaults.DELETED_CLOCK_COLUMN + "=" + DBDefaults
			.CLOCK_VALUE_PLACEHOLDER;

	public static String generateInsertStatement(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder();

		buffer.append("INSERT INTO ");
		buffer.append(row.getTable().getName());
		buffer.append(" (");

		Iterator<FieldValue> fieldsValuesIt = row.getFieldValues().iterator();

		while(fieldsValuesIt.hasNext())
		{
			FieldValue fValue = fieldsValuesIt.next();
			buffer.append(fValue.getDataField().getFieldName());
			valuesBuffer.append(fValue.getFormattedValue());

			if(fieldsValuesIt.hasNext())
			{
				buffer.append(",");
				valuesBuffer.append(",");
			}
		}

		buffer.append(") VALUES (");
		buffer.append(valuesBuffer.toString());
		buffer.append(")");

		return buffer.toString();
	}

	public static String generateUpdateStatement(Row row)
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<FieldValue> fieldsValuesIt = row.getFieldValues().iterator();

		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");

		while(fieldsValuesIt.hasNext())
		{
			FieldValue fValue = fieldsValuesIt.next();

			buffer.append(fValue.getDataField().getFieldName());
			buffer.append("=");
			buffer.append(fValue.getFormattedValue());

			if(fieldsValuesIt.hasNext())
				buffer.append(",");
		}

		if(buffer.charAt(buffer.length() - 1) == ',')
			buffer.setLength(buffer.length() - 1);
		return buffer.toString();

	}

	public static String generateDeleteStatement(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	public static String generateContentUpdateFunctionClause(ExecutionPolicy policy)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")");

		if(policy == ExecutionPolicy.DELETEWINS)
			buffer.append(">0");
		else
			buffer.append(">=0");

		return buffer.toString();
	}

	public static String generateVisibilityUpdateFunctionClause(ExecutionPolicy policy)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")");

		if(policy == ExecutionPolicy.DELETEWINS)
			buffer.append(">=0");
		else
			buffer.append(">0");

		return buffer.toString();
	}

	/**
	 * Generates a SQL statement that makes sure that the parnet row is re-inserted back.
	 * It does so silenty, which means that this statement will leave no footprint. In other words, no one will know
	 * that this statement was executed.
	 *
	 * @param parentRow
	 *
	 * @return
	 */
	public static String generateInsertBackParentRow(Row parentRow)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(parentRow.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_NOT_DELETED_EXPRESSION);

		// make sure the old field values that belong to the foreign key in the parent have the correct value in case
		// some delete set null as occured

		for(FieldValue fValue : parentRow.getFieldValues())
		{
			DataField field = fValue.getDataField();

			if(field.hasChilds())
			{
				buffer.append(",");
				buffer.append(field.getFieldName());
				buffer.append("=");
				buffer.append(fValue.getFormattedValue());
			}
		}

		buffer.append(" WHERE ");
		buffer.append(parentRow.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	public static String generateSetRowVisibleStatement(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_NOT_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	public String generateDeleteCascade(Row parentRow, ForeignKeyConstraint fkConstraint)
	{
		StringBuilder buffer = new StringBuilder();
		//TODO: to complete and review

		if(fkConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.DELETEWINS)
		{
			buffer.append("UPDATE ");
			buffer.append(fkConstraint.getChildTable().getName());
			buffer.append(" SET ");
			buffer.append(SET_DELETED_CLOCK_EXPRESION);

			Iterator<ParentChildRelation> relationsIt = fkConstraint.getFieldsRelations().iterator();

			while(relationsIt.hasNext())
			{
				ParentChildRelation relation = relationsIt.next();
				buffer.append(relation.getChild().getFieldName());
				buffer.append("=");
				buffer.append(parentRow.getFieldValue(relation.getParent().getFieldName()).getFormattedValue());

				if(relationsIt.hasNext())
					buffer.append(" AND ");
			}

			//delete all childs from state
			// this op depends on the local state, which is intended because in a DELETE WINS semantic, every
			// concurrent child should be erased.
		} else
		{

		}

		return buffer.toString();
	}

	// intercepts update operation and make it deterministic
	public static String[] makeToDeterministic(Connection con, CCJSqlParserManager parser, String sqlQuery)
			throws JSQLParserException
	{
		String[] deterQueries = null;

		// contains current_time_stamp
		// contains NOW(), use the same
		// contains select, do the select first
		// contains delete from where (not specify by full primary key)
		// fill in default value and IDs for insert
		net.sf.jsqlparser.statement.Statement sqlStmt = parser.parse(new StringReader(sqlQuery));
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
			replaceSelectionForInsert(con, parser, insertStmt, valList);
			//call function to replace and fill in the missing fields
			fillInMissingValue(tableName, colList, valList);

			if(colList.size() != valList.size())
			{
				LOG.error("cols and vals size must match before appending scratchpad values");
				RuntimeUtils.throwRunTimeException("cols and vals size dont match", ExitCode.ERRORTRANSFORM);
			}

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
			replaceSelectionForUpdate(con, parser, updateStmt, valList);
			//replace database functions like now or current time stamp

			//FIXME: we changed this next line of code because of the new version of sql parser
			//replaceValueForDatabaseFunctions(updateStmt.getTable().getName(), valList);
			replaceValueForDatabaseFunctions(updateStmt.getTables().get(0).getName(), valList);
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query
			deterQueries = fillInMissingPrimaryKeysForUpdate(con, updateStmt, colList, valList);

		} else if(sqlStmt instanceof Delete)
		{
			Delete deleteStmt = (Delete) sqlStmt;
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query
			deterQueries = fillInMissingPrimaryKeysForDelete(con, deleteStmt);
		}

		return deterQueries;
	}

	private static Set<String> findMissingDataFields(String tableName, List<String> colList, List<String> valueList)
	{
		if(DB_METADATA == null)
			if(Configuration.DEBUG_ENABLED)
				LOG.debug("DATABASE METADATA IS NULL");

		DatabaseTable dtB = DB_METADATA.getTable(tableName);

		if(dtB == null)
			if(Configuration.DEBUG_ENABLED)
				LOG.debug("dtb is null!!");
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
	private static void fillInMissingValue(String tableName, List<String> colList, List<String> valueList)
	{

		Set<String> missFields = findMissingDataFields(tableName, colList, valueList);

		//		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = DB_METADATA.getTable(tableName);

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
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(DATE_FORMAT) + "'");
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
								tableName, dF.getDataField())));*/
						throw new RuntimeException("The primary keys' values should not be missing");
					}
				} else
				{
					if(dF.isAutoIncrement())
					{
						int nextId = IdentifierFactory.getNextId(dF);
						valueList.add(String.valueOf(nextId));
					} else if(dF.getDefaultValue() == null)
					{
						valueList.add(RuntimeUtils.getDefaultValueForDataField(DATE_FORMAT, dF));
					} else
					{
						if(dF.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP"))
						{
							valueList.add("'" + DatabaseFunction.CURRENTTIMESTAMP(DATE_FORMAT) + "'");
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
	private static void replaceValueForDatabaseFunctions(String tableName, List<String> valueList)
	{
		for(int i = 0; i < valueList.size(); i++)
		{
			String valStr = valueList.get(i).trim();
			if(valStr.equalsIgnoreCase("NOW()") || valStr.equalsIgnoreCase("NOW") || valStr.equalsIgnoreCase(
					"CURRENT_TIMESTAMP") || valStr.equalsIgnoreCase("CURRENT_TIMESTAMP()") || valStr.equalsIgnoreCase(
					"CURRENT_DATE"))
			{
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(DATE_FORMAT) + "'");
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
	private static boolean isPrimaryKeyMissingFromWhereClause(String tableName, Expression whereClause)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = DB_METADATA.getTable(tableName);
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
	private static String getPrimaryKeySelectionQuery(String tableName, Expression whereClause)
	{
		//		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = DB_METADATA.getTable(tableName);

		return dbT.generatedPrimaryKeyQuery(whereClause.toString());
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
	private static String[] fillInMissingPrimaryKeysForUpdate(Connection con, Update updateStmt, List<String> colList,
													   List<String> valList)
	{
		String[] newUpdates = null;

		if(isPrimaryKeyMissingFromWhereClause(updateStmt.getTables().get(0).getName(), updateStmt.getWhere()))
		{
			String primaryKeySelectStr = getPrimaryKeySelectionQuery(updateStmt.getTables().get(0).getName(),
					updateStmt.getWhere());
			//executeUpdate the primaryKeySelectStr
			try
			{
				if(Configuration.TRACE_ENABLED)
					LOG.trace("fetching rows from main database");
				PreparedStatement sPst = con.prepareStatement(primaryKeySelectStr);
				ResultSet rs = sPst.executeQuery();
				newUpdates = assembleUpdates(updateStmt.getTables().get(0).getName(), colList, valList, rs);
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
			newUpdates[0] = assembleUpdate(updateStmt.getTables().get(0).getName(), colList, valList,
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
	private static String[] fillInMissingPrimaryKeysForDelete(Connection con, Delete delStmt)
	{
		String[] newDeletes = null;
		if(isPrimaryKeyMissingFromWhereClause(delStmt.getTable().getName(), delStmt.getWhere()))
		{
			String primaryKeySelectStr = getPrimaryKeySelectionQuery(delStmt.getTable().getName(),
					delStmt.getWhere());
			//executeUpdate the primaryKeySelectStr
			try
			{
				PreparedStatement sPst = con.prepareStatement(primaryKeySelectStr);
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
	private static void replaceSelectionForInsert(Connection con, CCJSqlParserManager parser, Insert insertStmt,
										   List<String> valList) throws JSQLParserException
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
				PlainSelect plainSelect = ((PlainSelect) ((Select) parser.parse(
						new StringReader(valStr))).getSelectBody());
				int selectItemCount = plainSelect.getSelectItems().size();
				PreparedStatement sPst;
				try
				{
					sPst = con.prepareStatement(valStr);
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
	private static void replaceSelectionForUpdate(Connection con, CCJSqlParserManager parser, Update upStmt,
										   List<String> valList) throws JSQLParserException
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
				PlainSelect plainSelect = ((PlainSelect) ((Select) parser.parse(
						new StringReader(valStr))).getSelectBody());
				assert (plainSelect.getSelectItems().size() == 1);
				try
				{
					PreparedStatement sPst = con.prepareStatement(valStr);
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
	private static String assembleInsert(String tableName, List<String> colList, List<String> valList)
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
				buffer.append(get_Value_In_Correct_Format(tableName, colList.get(i), valList.get(i)) + ",");
			}
		} else
		{
			for(int i = 0; i < valList.size(); i++)
			{
				buffer.append(get_Value_In_Correct_Format(tableName, i, valList.get(i)) + ",");
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
	private static String assembleUpdate(String tableName, List<String> colList, List<String> valList, String whereClauseStr)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("update ");
		buffer.append(tableName + " set ");
		for(int i = 0; i < colList.size(); i++)
		{
			buffer.append(colList.get(i) + " = ");
			buffer.append(OperationTransformer.get_Value_In_Correct_Format(tableName, colList.get(i), valList.get(i)) + ",");
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
	private static String[] assembleUpdates(String tableName, List<String> colList, List<String> valList, ResultSet rs)
	{
		if(Configuration.TRACE_ENABLED)
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
		DatabaseTable dbT = DB_METADATA.getTable(tableName);

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
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.ERRORTRANSFORM);
		}

		if(Configuration.TRACE_ENABLED)
			LOG.trace("update query assembled correctly");
		return updateStrList.toArray(new String[updateStrList.size()]);
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
	private static String[] assembleDeletes(String tableName, ResultSet rs)
	{
		if(Configuration.TRACE_ENABLED)
			LOG.trace("assembling deletes for table {}", tableName);

		StringBuilder buffer = new StringBuilder();
		String deleteMainBody;
		buffer.append("delete from ");
		buffer.append(tableName);
		buffer.append(" where ");
		deleteMainBody = buffer.toString();
		//Debug.println("Newly generated delete mainbody is " + deleteMainBody);

		//DatabaseTable dbTable = annotatedTableSchema.get(tableName);
		DatabaseTable dbTable = DB_METADATA.getTable(tableName);

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
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.ERRORTRANSFORM);
		}
		if(Configuration.TRACE_ENABLED)
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
	private static String get_Value_In_Correct_Format(String tableName, int fieldIndex, String Value)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = DB_METADATA.getTable(tableName);

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
	private static String get_Value_In_Correct_Format(String tableName, String dataFileName, String Value)
	{
		//DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DatabaseTable dbT = DB_METADATA.getTable(tableName);
		DataField dF = dbT.getField(dataFileName);
		return dF.get_Value_In_Correct_Format(Value);
	}
}
