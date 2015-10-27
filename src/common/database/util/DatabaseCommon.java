package common.database.util;


import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.value.FieldValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;


/**
 * Created by dnlopes on 11/05/15.
 */
public class DatabaseCommon
{

	final static String[] dataTypeList = {"INT", "FLOAT", "DOUBLE", "BOOL", "BOOLEAN", "DATE", "DATETIME",
			"TIMESTAMP", "CHAR", "VARCHAR", "REAL", "INTEGER", "TEXT", "smallint", "decimal", "tinyint", "bigint",
			"datetime",};

	public static PrimaryKeyValue getPrimaryKeyValue(final ResultSet rs, DatabaseTable dbTable) throws SQLException
	{
		PrimaryKeyValue pkValue = new PrimaryKeyValue(dbTable.getName());
		PrimaryKey pk = dbTable.getPrimaryKey();

		for(DataField field : pk.getPrimaryKeyFields().values())
		{
			String fieldValue = rs.getObject(field.getFieldName()).toString();

			if(fieldValue == null)
				throw new SQLException(("primary key cannot be null"));

			FieldValue fValue = new FieldValue(field, fieldValue);
			pkValue.addFieldValue(fValue);
		}
		return pkValue;
	}

	public static Row getFullRow(ResultSet rs, DatabaseTable dbTable) throws SQLException
	{
		PrimaryKeyValue pkValue = getPrimaryKeyValue(rs, dbTable);
		Row row = new Row(dbTable, pkValue);

		for(DataField field : dbTable.getNormalFields().values())
		{
			Object fieldValue = rs.getObject(field.getFieldName());
			String objString;
			if(fieldValue == null)
				objString = "NULL";
			else
				objString = fieldValue.toString();

			FieldValue fValue = new FieldValue(field, objString);
			row.addFieldValue(fValue);
		}

		return row;
	}

	public static Date NOW()
	{
		return new Date();
	}

	public static String CURRENTTIMESTAMP(DateFormat dateFormat)
	{
		return dateFormat.format(NOW());
	}

	/**
	 * Gets the _ data_ type.
	 *
	 * @param defStr
	 * 		the def str
	 *
	 * @return the _ data_ type
	 */
	public static String getDataType(String defStr)
	{
		String[] subStrs = defStr.split(" ");
		for(int i = 0; i < subStrs.length; i++)
		{
			String typeStr = subStrs[i];
			int endIndex = subStrs[i].indexOf("(");
			if(endIndex != -1)
				typeStr = subStrs[i].substring(0, endIndex);
			for(int j = 0; j < dataTypeList.length; j++)
			{
				if(typeStr.toUpperCase().equalsIgnoreCase(dataTypeList[j]))
					return dataTypeList[j];
			}
		}
		return "";
	}

	/**
	 * Fill the row values with the values of the current entry of the ResultSet
	 *
	 * @param row
	 * @param rs
	 *
	 * @throws SQLException
	 */
	public static void fillNormalFields(Row row, final ResultSet rs) throws SQLException
	{
		for(DataField field : row.getTable().getFieldsList())
		{
			if(field.isHiddenField())
				continue;

			String fieldValue = rs.getObject(field.getFieldName()).toString();

			if(fieldValue == null)
				throw new SQLException(("field value should not be null"));

			row.addFieldValue(new FieldValue(field, fieldValue));
		}
	}
}
