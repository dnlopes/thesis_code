package util.parser;

import database.util.field.LWW_BOOLEAN;
import database.util.field.LWW_DATETIME;
import database.util.field.LWW_DOUBLE;
import database.util.field.LWW_FLOAT;
import database.util.field.LWW_INTEGER;
import database.util.field.LWW_STRING;
import database.util.field.NONCRDT_Data_Field;
import database.util.field.NORMAL_BOOLEAN;
import database.util.field.NORMAL_DATETIME;
import database.util.field.NORMAL_DOUBLE;
import database.util.field.NORMAL_FLOAT;
import database.util.field.NORMAL_INTEGER;
import database.util.field.NORMAL_STRING;
import database.util.field.NUMDELTA_DATETIME;
import database.util.field.NUMDELTA_DOUBLE;
import database.util.field.NUMDELTA_FLOAT;
import database.util.field.NUMDELTA_INTEGER;
import database.util.CrdtDataFieldType;
import database.util.DataField;
import database.util.DatabaseDictionary;
import util.ExitCode;


/**
 * The Class DataFieldParser.
 */
public class DataFieldParser
{

	/**
	 * Create_ data_ field_ instance.
	 *
	 * @param tableName    the table name
	 * @param attributeDef the attribute def
	 * @param position     the position
	 *
	 * @return the data field
	 */
	public static DataField createField(String tableName, String attributeDef, int position)
	{
		attributeDef = attributeDef.trim();// remove the empty space at the
		// beginning and the end

		CrdtDataFieldType crdtType = getFieldType(attributeDef);
		String fieldName = getFieldName(attributeDef);
		String dataType = getFieldDataType(attributeDef);
		boolean isPrimaryKey = isPrimaryKey(attributeDef);
		boolean isAutoIncremantal = isAutoIncremental(attributeDef);

		DataField field = null;
		switch(crdtType)
		{
		case NONCRDTFIELD:
			field = new NONCRDT_Data_Field(fieldName, tableName, dataType, isPrimaryKey,
					isAutoIncremantal, position);
			break;
		case LWWINTEGER:
			field = new LWW_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case LWWFLOAT:
			field = new LWW_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case LWWDOUBLE:
			field = new LWW_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case LWWSTRING:
			field = new LWW_STRING(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case LWWDATETIME:
			field = new LWW_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case LWWBOOLEAN:
			field = new LWW_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NUMDELTAINTEGER:
			field = new NUMDELTA_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NUMDELTAFLOAT:
			field = new NUMDELTA_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NUMDELTADOUBLE:
			field = new NUMDELTA_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NUMDELTADATETIME:
			field = new NUMDELTA_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALINTEGER:
			field = new NORMAL_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALFLOAT:
			field = new NORMAL_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALDOUBLE:
			field = new NORMAL_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALSTRING:
			field = new NORMAL_STRING(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALDATETIME:
			field = new NORMAL_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
			break;
		case NORMALBOOLEAN:
			field = new NORMAL_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal,
					position);
		default:
			try
			{
				throw new RuntimeException("Unknown CRDT data types!" + crdtType);
			} catch(RuntimeException e)
			{
				e.printStackTrace();
				System.exit(ExitCode.UNKNOWNDATAFIELDANNOTYPE);
			}
		}
		setDefaultValue(field, attributeDef);
		return field;
	}


	/**
	 * Gets the _ annotation_ type.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return the _ annotation_ type
	 */
	public static String getAnnotationType(String attributeDef)
	{

		int startIndex = attributeDef.indexOf("@");
		if(startIndex == - 1)
			return "";
		int endIndex = attributeDef.indexOf(" ");
		if(endIndex <= startIndex)
		{
			throw_Wrong_Format_Exception(attributeDef);
		}

		return attributeDef.substring(startIndex + 1, endIndex);
	}

	/**
	 * Checks if is _ annotated.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return true, if is _ annotated
	 */
	private static boolean isAnnotated(String attributeDef)
	{
		int startIndex = attributeDef.indexOf("@");
		if(startIndex == - 1)
			return false;
		return true;
	}

	/**
	 * Gets the _ data_ field_ type.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return the _ data_ field_ type
	 */
	public static CrdtDataFieldType getFieldType(String attributeDef)
	{
		String annotationStr = DataFieldParser.getAnnotationType(attributeDef);
		if(annotationStr.equals(""))
		{
			return CrdtDataFieldType.NONCRDTFIELD;
		}
		return CrdtDataFieldType.valueOf(annotationStr);
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return the _ data_ field_ name
	 */
	public static String getFieldName(String attributeDef)
	{
		String[] subStrs = attributeDef.split("\\s");
		if(isAnnotated(attributeDef))
		{
			return subStrs[1].replaceAll("`", "");
		}
		return subStrs[0].replaceAll("`", "");
	}

	/**
	 * Gets the _ data_ type.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return the _ data_ type
	 */
	public static String getFieldDataType(String attributeDef)
	{
		String dataType = DatabaseDictionary.getDataType(attributeDef);
		if(dataType.equals(""))
		{
			throw_Wrong_Format_Exception(attributeDef);
		}
		return dataType;
	}

	/**
	 * Checks if is _ primary_ key.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return true, if is _ primary_ key
	 */
	public static boolean isPrimaryKey(String attributeDef)
	{
		return attributeDef.toUpperCase().contains("PRIMARY KEY");
	}

	/**
	 * Checks if is _ auto incremental.
	 *
	 * @param attributeDef the attribute def
	 *
	 * @return true, if is _ auto incremental
	 */
	public static boolean isAutoIncremental(String attributeDef)
	{
		return attributeDef.toUpperCase().contains("AUTO_INCREMENT");
	}

	/**
	 * Set_ default_ value.
	 *
	 * @param field        the d f
	 * @param attributeDef the attribute def
	 */
	public static void setDefaultValue(DataField field, String attributeDef)
	{

		if(attributeDef.toUpperCase().contains("DEFAULT"))
		{
			int startIndex = attributeDef.toUpperCase().indexOf("DEFAULT");
			String defaultValue = attributeDef.substring(startIndex + 8);
			defaultValue.replaceAll("'", "");
			field.setDefaultValue(defaultValue);
		}
		if(attributeDef.toUpperCase().contains("NULL") && ! attributeDef.toUpperCase().contains("NOT NULL"))
		{
			field.setDefaultValue("NULL");
		}
	}

	/**
	 * Throw_ wrong_ format_ exception.
	 *
	 * @param schemaStr the schema str
	 */
	private static void throw_Wrong_Format_Exception(String schemaStr)
	{
		try
		{
			throw new RuntimeException("The attribute defintion " + schemaStr + " is in a wrong format!");
		} catch(RuntimeException e)
		{
			e.printStackTrace();
			System.exit(ExitCode.WRONGCREATTABLEFORMAT);
		}
	}
}
