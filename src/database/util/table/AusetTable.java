package database.util.table;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import database.util.CrdtTableType;
import database.util.DataField;
import database.util.DatabaseTable;
import util.debug.Debug;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;


/**
 * The Class AusetTable.
 */
public class AusetTable extends DatabaseTable
{

	/**
	 * Instantiates a new aoset table.
	 *
	 * @param tableName  the t n
	 * @param dataFields the d hm
	 */
	public AusetTable(String tableName, LinkedHashMap<String, DataField> dataFields)
	{
		super(tableName, CrdtTableType.AUSETTABLE, dataFields);
	}

	/**
	 * @param insertStatement
	 * @param insertQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see database.util.DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 */
	public String[] transform_Insert(Insert insertStatement, String insertQuery) throws JSQLParserException
	{
		// get tableName
		String tbName = insertStatement.getTable().getName();
		// get column list, if not empty, please add deletedflag, causality and
		// lwwts
		List colList = insertStatement.getColumns();
		// get value list, append these three into it
		String valueStr;

		int startIndex = insertQuery.toUpperCase().indexOf("VALUE");
		startIndex = insertQuery.indexOf("(", startIndex);
		int endIndex = insertQuery.lastIndexOf(")");
		valueStr = insertQuery.substring(startIndex + 1, endIndex);

		StringBuffer buffer = new StringBuffer();
		buffer.append("insert into ");
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
		buffer.append(valueStr + ",");
		buffer.append(deletedField.getDefaultValue() + ",");
		buffer.append("? ,"); // for causality clock
		buffer.append("?");// for lww timestamp
		buffer.append(");");

		Debug.println("This is transformed query for AOSET insert: " + buffer.toString());
		String[] transformedSqls = new String[1];
		transformedSqls[0] = buffer.toString();
		return transformedSqls;
	}

	/**
	 * @return
	 *
	 * @see database.util.DatabaseTable#toString()
	 */
	public String toString()
	{
		return super.toString();
	}

	/**
	 * @param rs
	 * @param updateStatement
	 * @param updateQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see database.util.DatabaseTable#transform_Update(java.sql.ResultSet, net.sf.jsqlparser.statement.update.Update,
	 * java.lang.String)
	 */
	@Override
	public String[] transform_Update(ResultSet rs, Update updateStatement, String updateQuery)
			throws JSQLParserException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param deleteStatement
	 * @param deleteQuery
	 *
	 * @return
	 *
	 * @throws JSQLParserException
	 * @see database.util.DatabaseTable#transform_Delete(net.sf.jsqlparser.statement.delete.Delete, java.lang.String)
	 */
	@Override
	public String[] transform_Delete(Delete deleteStatement, String deleteQuery) throws JSQLParserException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
