package weaql.common.parser;


import weaql.common.database.util.*;
import weaql.common.database.field.*;
import weaql.common.database.value.NullFieldValue;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import weaql.common.util.RuntimeUtils;
import weaql.common.util.ExitCode;


/**
 * The Class DataFieldParser.
 */
public class DataFieldParser
{

	public static DataField createField(String tableName, String attributeDef, int position)
	{
		attributeDef = attributeDef.trim();

		String[] splitted = splitAnnotationsFromField(attributeDef);
		String annotations = splitted[0];
		String fieldDefinition = splitted[1];

		CrdtDataFieldType crdtType = getFieldCRDTType(annotations);
		SemanticPolicy semanticPolicy = getSemanticPolicy(annotations);
		String fieldName = getFieldName(fieldDefinition);
		String dataType = getFieldDataType(fieldDefinition);
		boolean isPrimaryKey = isPrimaryKey(attributeDef);
		boolean isAutoIncremantal = isAutoIncremental(attributeDef);

		DataField field = null;
		switch(crdtType)
		{
		case NONCRDTFIELD:
			field = new NONCRDT_Data_Field(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWINTEGER:
			field = new LWW_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWFLOAT:
			field = new LWW_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWDOUBLE:
			field = new LWW_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWSTRING:
			field = new LWW_STRING(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWDATETIME:
			field = new LWW_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case LWWBOOLEAN:
			field = new LWW_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NUMDELTAINTEGER:
			field = new NUMDELTA_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NUMDELTAFLOAT:
			field = new NUMDELTA_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NUMDELTADOUBLE:
			field = new NUMDELTA_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NUMDELTADATETIME:
			field = new NUMDELTA_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALINTEGER:
			field = new NORMAL_INTEGER(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALFLOAT:
			field = new NORMAL_FLOAT(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALDOUBLE:
			field = new NORMAL_DOUBLE(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALSTRING:
			field = new NORMAL_STRING(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALDATETIME:
			field = new NORMAL_DATETIME(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
			break;
		case NORMALBOOLEAN:
			field = new NORMAL_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey, isAutoIncremantal, position,
					semanticPolicy);
		default:
			RuntimeUtils.throwRunTimeException("unknown CRDT data type:" + crdtType, ExitCode.SCHEMANOCRDTTABLE);
		}

		setDefaultValue(field, fieldDefinition);
		return field;
	}

	private static CrdtDataFieldType getFieldCRDTType(String annotations)
	{
		int numberOfAnnotations = StringUtils.countMatches(annotations, "@");

		if(numberOfAnnotations == 0) // no annotations, use default
			return CrdtDataFieldType.NONCRDTFIELD;
		else if(numberOfAnnotations == 1)
		{
			String annotation = StringUtils.substring(annotations, 1);
			if(EnumUtils.isValidEnum(CrdtDataFieldType.class, annotation))
				return CrdtDataFieldType.valueOf(StringUtils.substring(annotations, 1));
			else
				return CrdtDataFieldType.NONCRDTFIELD;
		} else
		{
			String[] splittedAnnotations = StringUtils.split(annotations, " ");
			if(EnumUtils.isValidEnum(CrdtDataFieldType.class, StringUtils.substring(splittedAnnotations[0], 1)))
				return CrdtDataFieldType.valueOf(StringUtils.substring(splittedAnnotations[0], 1));
			else if(EnumUtils.isValidEnum(CrdtDataFieldType.class, StringUtils.substring(splittedAnnotations[1], 1)))
				return CrdtDataFieldType.valueOf(StringUtils.substring(splittedAnnotations[1], 1));
			else
				RuntimeUtils.throwRunTimeException("unexpected annotation for CrdtDataFieldType",
						ExitCode.INVALIDUSAGE);
		}

		return null;
	}

	private static SemanticPolicy getSemanticPolicy(String annotations)
	{
		int numberOfAnnotations = StringUtils.countMatches(annotations, "@");

		if(numberOfAnnotations == 0) // no annotations, use default
			return SemanticPolicy.SEMANTIC;
		else if(numberOfAnnotations == 1)
		{
			String annotation = StringUtils.substring(annotations, 1);
			if(EnumUtils.isValidEnum(SemanticPolicy.class, annotation))
				return SemanticPolicy.valueOf(StringUtils.substring(annotations, 1));
			else
				return SemanticPolicy.SEMANTIC;
		} else
		{
			String[] splittedAnnotations = StringUtils.split(annotations, " ");
			if(EnumUtils.isValidEnum(SemanticPolicy.class, StringUtils.substring(splittedAnnotations[0], 1)))
				return SemanticPolicy.valueOf(StringUtils.substring(splittedAnnotations[0], 1));
			else if(EnumUtils.isValidEnum(SemanticPolicy.class, StringUtils.substring(splittedAnnotations[1], 1)))
				return SemanticPolicy.valueOf(StringUtils.substring(splittedAnnotations[1], 1));
			else
				RuntimeUtils.throwRunTimeException("unexpected annotation for SemanticPolicy", ExitCode.INVALIDUSAGE);
		}

		return null;
	}

	private static String[] splitAnnotationsFromField(String fieldDefinition)
	{
		int numberOfAnnotations = StringUtils.countMatches(fieldDefinition, "@");
		int whiteSpaceIndex = 0;

		if(numberOfAnnotations == 0)
			return new String[]{"", fieldDefinition.trim()};
		else if(numberOfAnnotations == 1)
			whiteSpaceIndex = StringUtils.ordinalIndexOf(fieldDefinition, " ", 1);
		else if(numberOfAnnotations == 2)
			whiteSpaceIndex = StringUtils.ordinalIndexOf(fieldDefinition, " ", 2);
		else
			RuntimeUtils.throwRunTimeException("unexpected number of annotations", ExitCode.SCHEMANOCRDTTABLE);

		String annotations = StringUtils.left(fieldDefinition, whiteSpaceIndex).trim();
		String fieldData = StringUtils.right(fieldDefinition, fieldDefinition.length() - whiteSpaceIndex).trim();

		return new String[]{annotations, fieldData};
	}

	private static String getFieldName(String fieldDefinition)
	{
		fieldDefinition = fieldDefinition.trim();

		int whiteSpaceIndex = StringUtils.indexOf(fieldDefinition, " ");
		return StringUtils.left(fieldDefinition, whiteSpaceIndex);
	}

	public static String getFieldDataType(String attributeDef)
	{
		String dataType = DatabaseCommon.getDataType(attributeDef);
		if(dataType.equals(""))
		{
			throw_Wrong_Format_Exception(attributeDef);
		}
		return dataType;
	}

	public static boolean isPrimaryKey(String attributeDef)
	{
		return attributeDef.toUpperCase().contains("PRIMARY KEY");
	}

	public static boolean isAutoIncremental(String attributeDef)
	{
		return attributeDef.toUpperCase().contains("AUTO_INCREMENT");
	}

	public static void setDefaultValue(DataField field, String attributeDef)
	{
		if(attributeDef.toUpperCase().contains("DEFAULT"))
		{
			if(attributeDef.toUpperCase().contains("DEFAULT NULL"))
			{
				NullFieldValue nullFieldValue = new NullFieldValue(field);
				field.setDefaultFieldValue(nullFieldValue);
			} else
				RuntimeUtils.throwRunTimeException("custom default value capture logic not yet implemented",
						ExitCode.MISSING_IMPLEMENTATION);
		}
	}

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
