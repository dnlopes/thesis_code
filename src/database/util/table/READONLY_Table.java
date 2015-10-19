package database.util.table;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import database.util.ExecutionPolicy;
import database.util.field.CrdtDataFieldType;
import database.util.field.DataField;
import util.ExitCode;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;


/**
 * The Class READONLY_Table.
 */
public class READONLY_Table extends DatabaseTable
{

	/**
	 * Instantiates a new rEADONL y_ table.
	 *
	 * @param tableName  the t n
	 * @param dataFields the d hm
	 */
	public READONLY_Table(String tableName, LinkedHashMap<String, DataField> dataFields, ExecutionPolicy policy)
	{
		super(tableName, CRDTTableType.NONCRDTTABLE, dataFields, policy);
		for(Map.Entry<String, DataField> entry : dataFields.entrySet())
		{
			DataField df = entry.getValue();
			CrdtDataFieldType crdtType = df.getCrdtType();
			if(! (crdtType == CrdtDataFieldType.NONCRDTFIELD || crdtType == CrdtDataFieldType.NORMALBOOLEAN || crdtType == CrdtDataFieldType.NORMALDATETIME || crdtType == CrdtDataFieldType.NORMALDOUBLE || crdtType == CrdtDataFieldType.NORMALFLOAT || crdtType == CrdtDataFieldType.NORMALINTEGER || crdtType == CrdtDataFieldType.NORMALSTRING))
			{
				try
				{
					throw new RuntimeException("Attributes in a readonly table should not be annotated!");
				} catch(RuntimeException e)
				{
					e.printStackTrace();
					System.exit(ExitCode.READONLYTBLWRONGANNO);
				}
			}
		}
	}

	/**
	 * @return
	 *
	 * @see DatabaseTable#toString()
	 */
	public String toString()
	{
		return super.toString();
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
	@Override
	public String[] transform_Insert(Insert insertStatement, String insertQuery) throws JSQLParserException
	{
		return null;
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
	@Override
	public String[] transform_Update(ResultSet rs, Update updateStatement, String updateQuery)
			throws JSQLParserException
	{
		return null;
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
	@Override
	public String[] transform_Delete(Delete deleteStatement, String deleteQuery) throws JSQLParserException
	{
		return null;
	}
}
