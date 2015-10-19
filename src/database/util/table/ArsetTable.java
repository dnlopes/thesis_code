package database.util.table;

import java.sql.ResultSet;
import java.util.*;

import database.util.ExecutionPolicy;
import database.util.field.LWW_DELETEDFLAG;
import database.util.field.LWW_LOGICALTIMESTAMP;
import database.util.field.DataField;
import util.ExitCode;
import util.debug.Debug;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.*;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;


/**
 * The Class ArsetTable.
 */
public class ArsetTable extends DatabaseTable
{

	/**
	 * Instantiates a new arset table.
	 *
	 * @param tableName  the t n
	 * @param dataFields the d hm
	 */
	public ArsetTable(String tableName, LinkedHashMap<String, DataField> dataFields, ExecutionPolicy policy)
	{
		super(tableName, CRDTTableType.ARSETTABLE, dataFields, policy);
	}

	/**
	 * Gets the _ insert_ ingore_ stmt.
	 *
	 * @param tbName    the tb name
	 * @param colList   the col list
	 * @param valueList the value list
	 *
	 * @return the _ insert_ ingore_ stmt
	 */
	public String get_Insert_Ingore_Stmt(String tbName, List colList, String[] valueList)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("insert ignore into ");
		buffer.append(tbName + " ");
		Iterator it = colList.iterator();
		if(colList.size() > 0)
		{
			buffer.append("(");
			while(it.hasNext())
			{
				buffer.append(it.next() + ",");
			}
			buffer.append(deletedField.getFieldName() + ",");
			buffer.append(timestampField.getFieldName() + ",");
			buffer.append(timestampLWW.get_Data_Field_Name());
			buffer.append(") ");
		}

		buffer.append(" values (");
		for(int i = 0; i < valueList.length; i++)
		{
			buffer.append(valueList[i] + ",");
		}
		buffer.append(deletedField.getDefaultValue() + ",");
		buffer.append("? ,"); // for causality clock
		buffer.append("?");// for lww timestamp
		buffer.append(");");

		Debug.println("This is transformed query for ARSET insert: " + buffer.toString());
		return buffer.toString();
	}

	/**
	 * Gets the _ insert_ update.
	 *
	 * @param tbName         the tb name
	 * @param colList        the col list
	 * @param valueList      the value list
	 * @param whereClauseStr the where clause str
	 *
	 * @return the _ insert_ update
	 */
	public String[] get_Insert_Update(String tbName, Vector<String> colList, Vector<String> valueList,
									  String whereClauseStr)
	{
		// partition into two parts: lww or different
		StringBuffer nonLWWBuffer = new StringBuffer();
		StringBuffer LWWBuffer;

		nonLWWBuffer.append("update ");
		nonLWWBuffer.append(tbName + " ");
		nonLWWBuffer.append("set ");

		LWWBuffer = new StringBuffer(nonLWWBuffer.toString());
		int nonLWWBufferLength = nonLWWBuffer.length();

		for(int i = 0; i < colList.size(); i++)
		{
			Debug.println(colList.get(i));
			DataField dT = fieldsMap.get(colList.get(i));
			String dataValue = valueList.elementAt(i);
			if(dT.getCrdtType().name().contains("LWW"))
			{
				// apply last writer win
				switch(dT.getCrdtType())
				{
				case LWWINTEGER:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWFLOAT:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWDOUBLE:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWBOOLEAN:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWSTRING:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWDATETIME:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				default:
					try
					{
						throw new RuntimeException("No such LWW types" + dT.getCrdtType().toString());
					} catch(RuntimeException e)
					{
						e.printStackTrace();
						System.exit(ExitCode.UNKNOWNLWWDATATYPE);
					}
				}
			} else
			{
				// apply different strategies
				switch(dT.getCrdtType())
				{
				case NONCRDTFIELD:
					nonLWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case NUMDELTAINTEGER:
					nonLWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case NUMDELTAFLOAT:
					nonLWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case NUMDELTADOUBLE:
					nonLWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case NUMDELTADATETIME:
					nonLWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				default:
					try
					{
						throw new RuntimeException("No such NONLWW types" + dT.getCrdtType().toString());
					} catch(RuntimeException e)
					{
						e.printStackTrace();
						System.exit(ExitCode.UNKNOWNNONLWWDATATYPE);
					}
				}
			}
		}
		// appending the fields to the lww
		LWWBuffer.append(((LWW_DELETEDFLAG) deletedField).get_Unmark_Deleted() + ",");
		LWWBuffer.append(((LWW_LOGICALTIMESTAMP) timestampField).get_Set_Logical_Timestamp() + ",");
		LWWBuffer.append(timestampLWW.get_Set_Timestamp_LWW() + " ");

		// add lww clause to where
		LWWBuffer.append(whereClauseStr + " ");
		LWWBuffer.append("and " + timestampLWW.get_Set_LWW_Clause() + ";");

		String[] transformedSqls;
		if(nonLWWBuffer.length() > nonLWWBufferLength)
		{
			nonLWWBuffer.deleteCharAt(nonLWWBuffer.length() - 1);
			nonLWWBuffer.append(" " + whereClauseStr + ";");
			Debug.println(nonLWWBuffer.toString() + LWWBuffer.toString());
			transformedSqls = new String[2];
			transformedSqls[0] = nonLWWBuffer.toString();
			transformedSqls[1] = LWWBuffer.toString();
		} else
		{
			Debug.println(LWWBuffer.toString());
			transformedSqls = new String[1];
			transformedSqls[0] = LWWBuffer.toString();
		}
		return transformedSqls;
	}

	/**
	 * @param insertStatement
	 * @param insertQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 */
	public String[] transform_Insert(Insert insertStatement, String insertQuery) throws JSQLParserException
	{
		// change to insert ingore and update
		// get tableName, figure out the primary key
		String tbName = insertStatement.getTable().getName();
		// get column list, if not empty, please add deletedflag, causality and
		// lwwts
		List<Column> colList = insertStatement.getColumns();
		// get value list, append these three into it
		String valueStr;
		int startIndex = insertQuery.indexOf("values");
		startIndex = insertQuery.indexOf("(", startIndex);
		int endIndex = insertQuery.lastIndexOf(")");
		valueStr = insertQuery.substring(startIndex + 1, endIndex);

		String[] valueList = valueStr.split(",");

		// change to an insert ingore
		String insertIngoreStr = get_Insert_Ingore_Stmt(tbName, colList, valueList);

		// change to update
		//Set<String> dataFieldKeyList = fieldsMap.keySet();
		//Iterator dFKIt = dataFieldKeyList.iterator();
		StringBuilder whereClause = new StringBuilder(" where ");
		int whereClauseSize = whereClause.length();

		// remove primary key from the update set
		Vector<String> newColList = new Vector<>();
		Vector<String> newValueList = new Vector<>();
		for(int i = 0; i < colList.size(); i++)
		{
			DataField dT = fieldsMap.get(colList.get(i).toString());
			if(dT.isPrimaryKey())
			{
				whereClause.append(dT.getFieldName() + " = " + valueList[i] + " and ");
			} else
			{
				newColList.add(colList.get(i).toString());
				newValueList.add(valueList[i]);
			}
		}

		// change to update

		if(whereClause.length() == whereClauseSize)
		{
			try
			{
				throw new RuntimeException("This table " + tbName + " doesn't have primary keys!");
			} catch(RuntimeException e)
			{
				e.printStackTrace();
				System.exit(ExitCode.NONPRIMARYKEY);
			}
		} else
		{
			int removeIndex = whereClause.lastIndexOf("and");
			whereClause.delete(removeIndex, whereClause.length());
		}

		String[] updateStrs = get_Insert_Update(tbName, newColList, newValueList, whereClause.toString());

		String[] transformedSqls = new String[updateStrs.length + 1];
		transformedSqls[0] = insertIngoreStr;
		for(int i = 1; i < transformedSqls.length; i++)
		{
			transformedSqls[i] = updateStrs[i - 1];
		}

		Debug.println("Sqlquery " + insertQuery + " transformed to " + Arrays.toString(transformedSqls));

		return transformedSqls;
	}

	/**
	 * @param rs
	 * @param updateStatement
	 * @param updateQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see DatabaseTable#transform_Update(java.sql.ResultSet, net.sf.jsqlparser.statement.update.Update,
	 * java.lang.String)
	 */
	public String[] transform_Update(ResultSet rs, Update updateStatement, String updateQuery)
			throws JSQLParserException
	{

		String tbName = updateStatement.getTables().get(0).getName();

		List colList = updateStatement.getColumns();
		List valueList = updateStatement.getExpressions();

		int whereClauseIndex = updateQuery.toUpperCase().indexOf(" WHERE ");
		String whereClauseStr = updateQuery.substring(whereClauseIndex);

		// get where clause

		// partition into two parts: lww or different
		StringBuffer nonLWWBuffer = new StringBuffer();
		StringBuffer LWWBuffer;

		nonLWWBuffer.append("update ");
		nonLWWBuffer.append(tbName + " ");
		nonLWWBuffer.append("set ");

		LWWBuffer = new StringBuffer(nonLWWBuffer.toString());
		int nonLWWBufferLength = nonLWWBuffer.length();

		Iterator<Column> colIt = colList.iterator();
		Iterator valIt = valueList.iterator();

		while(colIt.hasNext())
		{
			Column cL = colIt.next();
			DataField dT = fieldsMap.get(cL.getColumnName());
			String dataValue = valIt.next().toString();
			if(dT.getCrdtType().name().contains("LWW"))
			{
				// apply last writer win
				switch(dT.getCrdtType())
				{
				case LWWINTEGER:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWFLOAT:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWDOUBLE:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWBOOLEAN:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWSTRING:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				case LWWDATETIME:
					LWWBuffer.append(dT.get_Crdt_Form(dataValue) + ",");
					break;
				default:
					try
					{
						throw new RuntimeException("No such LWW types" + dT.getCrdtType().toString());
					} catch(RuntimeException e)
					{
						e.printStackTrace();
						System.exit(ExitCode.UNKNOWNLWWDATATYPE);
					}

				}
			} else
			{
				// apply different strategies

				switch(dT.getCrdtType())
				{
				case NONCRDTFIELD:
					nonLWWBuffer.append(dT.get_Crdt_Form(rs, dataValue) + ",");
					break;
				case NUMDELTAINTEGER:
					nonLWWBuffer.append(dT.get_Crdt_Form(rs, dataValue) + ",");
					break;
				case NUMDELTAFLOAT:
					nonLWWBuffer.append(dT.get_Crdt_Form(rs, dataValue) + ",");
					break;
				case NUMDELTADOUBLE:
					nonLWWBuffer.append(dT.get_Crdt_Form(rs, dataValue) + ",");
					break;
				case NUMDELTADATETIME:
					nonLWWBuffer.append(dT.get_Crdt_Form(rs, dataValue) + ",");
					break;
				default:
					try
					{
						throw new RuntimeException("No such NONLWW types" + dT.getCrdtType().toString());
					} catch(RuntimeException e)
					{
						e.printStackTrace();
						System.exit(ExitCode.UNKNOWNNONLWWDATATYPE);
					}
				}

			}
		}

		// appending the fields to the lww
		LWWBuffer.append(((LWW_DELETEDFLAG) deletedField).get_Unmark_Deleted() + ",");
		LWWBuffer.append(((LWW_LOGICALTIMESTAMP) timestampField).get_Set_Logical_Timestamp() + ",");
		LWWBuffer.append(timestampLWW.get_Set_Timestamp_LWW() + " ");

		// add lww clause to where
		LWWBuffer.append(whereClauseStr + " ");
		LWWBuffer.append("and " + timestampLWW.get_Set_LWW_Clause() + ";");

		String[] transformedSqls;

		if(nonLWWBuffer.length() > nonLWWBufferLength)
		{
			nonLWWBuffer.deleteCharAt(nonLWWBuffer.length() - 1);
			nonLWWBuffer.append(" " + whereClauseStr + ";");
			Debug.println(nonLWWBuffer.toString() + LWWBuffer.toString());
			transformedSqls = new String[2];
			transformedSqls[0] = nonLWWBuffer.toString();
			transformedSqls[1] = LWWBuffer.toString();
		} else
		{
			Debug.println(LWWBuffer.toString());
			transformedSqls = new String[1];
			transformedSqls[0] = LWWBuffer.toString();
		}
		return transformedSqls;
	}

	/**
	 * @param deleteStatement
	 * @param deleteQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see DatabaseTable#transform_Delete(net.sf.jsqlparser.statement.delete.Delete, java.lang.String)
	 */
	public String[] transform_Delete(Delete deleteStatement, String deleteQuery) throws JSQLParserException
	{
		// change to an update
		// take table name
		// update delete_field = true and causality and timestamp where lww

		// first get tableName, assert there is a single table
		String tbName = deleteStatement.getTable().getName();
		Debug.println("get table name " + tbName);
		// then get where clause
		int whereClauseIndex = deleteQuery.toUpperCase().indexOf(" WHERE ");
		String whereClauseStr = deleteQuery.substring(whereClauseIndex);

		Debug.println("Here is a delete where clause!");

		StringBuffer buffer = new StringBuffer();
		buffer.append("update ");
		buffer.append(tbName + " ");
		buffer.append("set " + ((LWW_DELETEDFLAG) deletedField).get_Mark_Deleted() + ",");
		buffer.append(((LWW_LOGICALTIMESTAMP) timestampField).get_Set_Logical_Timestamp() + ",");
		buffer.append(timestampLWW.get_Set_Timestamp_LWW() + " ");

		// add lww clause to where
		buffer.append(whereClauseStr + " ");
		buffer.append("and " + timestampLWW.get_Set_LWW_Clause() + ";");

		Debug.println("ARSET delete transformation: " + buffer.toString());
		String[] transformedSqls = new String[1];
		transformedSqls[0] = buffer.toString();
		return transformedSqls;
	}

}
