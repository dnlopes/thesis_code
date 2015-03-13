/*
 * This class defines methods to parse a create table statement, for example
 * it will return the table name, and return attributes list.
 */
package database.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import database.invariants.ForeignKeyInvariant;
import database.invariants.Invariant;
import database.invariants.UniqueInvariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.debug.Debug;

import runtime.Runtime;

import database.util.table.AosetTable;
import database.util.table.ArsetTable;
import database.util.table.AusetTable;
import crdtlib.CrdtFactory;
import database.util.table.READONLY_Table;
import database.util.table.UosetTable;
import database.util.CrdtTableType;
import database.util.DataField;
import database.util.DatabaseTable;


/**
 * The Class CreateStatementParser.
 */
public class CreateStatementParser
{

	static final Logger LOG = LoggerFactory.getLogger(CreateStatementParser.class);

	/**
	 * Checks if is _ create_ table_ statement.
	 *
	 * @param schemaStr the schema str
	 *
	 * @return true, if is _ create_ table_ statement
	 */
	public static boolean is_Create_Table_Statement(String schemaStr)
	{
		return schemaStr.toLowerCase().contains("create table");
	}

	/**
	 * Create_ table_ instance.
	 *
	 * @param schemaStr the schema str
	 *
	 * @return the database table
	 */
	public static DatabaseTable createTable(String schemaStr)
	{
		if(! is_Create_Table_Statement(schemaStr))
			return null;
		else
		{
			String tableTitleStr = get_Table_Title_String(schemaStr);
			String bodyStr = get_Table_Body_String(schemaStr);
			CrdtTableType tableType = get_Table_Type(tableTitleStr);
			String tableName = get_Table_Name(tableTitleStr);
			LinkedHashMap<String, DataField> fieldsMap = createFields(tableName, bodyStr);

			DatabaseTable dT = null;

			switch(tableType)
			{
			case NONCRDTTABLE:
				dT = new READONLY_Table(tableName, fieldsMap);
				break;
			case AOSETTABLE:
				dT = new AosetTable(tableName, fieldsMap);
				break;
			case ARSETTABLE:
				ArsetTable.addLwwDeletedFlagDataField(tableName, fieldsMap);
				dT = new ArsetTable(tableName, fieldsMap);
				break;
			case UOSETTABLE:
				dT = new UosetTable(tableName, fieldsMap);
				break;
			case AUSETTABLE:
				dT = new AusetTable(tableName, fieldsMap);
				break;
			default:
				try
				{
					throw new RuntimeException("Unknown table annotation type");
				} catch(RuntimeException e)
				{
					e.printStackTrace();
					System.exit(ExitCode.UNKNOWNTABLEANNOTYPE);
				}
			}
			return dT;
		}
	}

	/**
	 * Gets the _ table_ title_ string.
	 *
	 * @param schemaStr the schema str
	 *
	 * @return the _ table_ title_ string
	 */
	public static String get_Table_Title_String(String schemaStr)
	{
		int endIndex = schemaStr.indexOf("(");
		return schemaStr.substring(0, endIndex).replaceAll("'", "");
	}

	/**
	 * Gets the _ table_ body_ string.
	 *
	 * @param schemaStr the schema str
	 *
	 * @return the _ table_ body_ string
	 */
	public static String get_Table_Body_String(String schemaStr)
	{
		int startIndex = schemaStr.indexOf("(");
		int endIndex = schemaStr.lastIndexOf(")");

		if(startIndex == - 1 || endIndex == - 1 || startIndex >= endIndex)
		{
			throw_Wrong_Format_Exception(schemaStr);
		}

		return schemaStr.substring(startIndex + 1, endIndex);
	}

	/**
	 * Gets the _ table_ type_ annotation.
	 *
	 * @param titleStr the title str
	 *
	 * @return the _ table_ type_ annotation
	 */
	private static String get_Table_Type_Annotation(String titleStr)
	{
		int startIndex = titleStr.indexOf("@");
		if(startIndex == - 1)
			return ""; // there is no annotation
		int endIndex = titleStr.indexOf(" ", startIndex);
		String annotationStr = titleStr.substring(startIndex + 1, endIndex);
		return annotationStr;
	}

	/**
	 * Gets the _ table_ type.
	 *
	 * @param titleStr the title str
	 *
	 * @return the _ table_ type
	 */
	public static CrdtTableType get_Table_Type(String titleStr)
	{

		String annotStr = get_Table_Type_Annotation(titleStr);
		if(annotStr.equals(""))
			return CrdtTableType.NONCRDTTABLE;
		else
			return CrdtTableType.valueOf(annotStr);

	}

	/**
	 * Gets the _ table_ name.
	 *
	 * @param titleStr the title str
	 *
	 * @return the _ table_ name
	 */
	public static String get_Table_Name(String titleStr)
	{
		if(! titleStr.toLowerCase().contains("table"))
		{
			throw_Wrong_Format_Exception(titleStr);
		}
		titleStr = titleStr.replaceAll("\\s+", " ");
		titleStr = titleStr.replaceAll("`", "");
		String[] subStrs = titleStr.split("\\s"); // \\s is space
		if(subStrs.length == 0)
		{
			throw_Wrong_Format_Exception(titleStr);
		}
		return subStrs[subStrs.length - 1];
	}

	/**
	 * Checks if is right comma to split.
	 *
	 * @param str        the str
	 * @param beginIndex the begin index
	 * @param commaIndex the comma index
	 *
	 * @return true, if is right comma to split
	 */
	private static boolean isRightCommaToSplit(String str, int beginIndex, int commaIndex)
	{
		int cursorIndex = commaIndex;
		int leftBracket = 0;
		int rightBracket = 0;
		while(cursorIndex >= beginIndex)
		{
			if(str.charAt(cursorIndex) == '(')
			{
				leftBracket++;
			} else if(str.charAt(cursorIndex) == ')')
			{
				rightBracket++;
			}
			cursorIndex--;
		}
		return leftBracket == rightBracket;
	}

	/**
	 * Gets the _ declarations.
	 *
	 * @param bodyStr the body str
	 *
	 * @return the _ declarations
	 */
	public static String[] getDeclarationsStrs(String bodyStr)
	{
		bodyStr = bodyStr.replaceAll("\\s+", " ");
		List<String> declarationList = new ArrayList<>();
		int beginIndex = 0;
		int declarationBeginIndex = 0;
		while(beginIndex < bodyStr.length())
		{
			int commaIndex = bodyStr.indexOf(',', beginIndex);
			if(commaIndex == - 1)
			{
				declarationList.add(bodyStr.substring(declarationBeginIndex));
				break;
			} else
			{
				if(isRightCommaToSplit(bodyStr, declarationBeginIndex, commaIndex))
				{
					declarationList.add(bodyStr.substring(declarationBeginIndex, commaIndex));
					beginIndex = commaIndex + 1;
					declarationBeginIndex = beginIndex;
				} else
				{
					//declarationBeginIndex = beginIndex;
					beginIndex = commaIndex + 1;
					continue;
				}
			}
		}
		//search for the comma
		//String[] subStrs = bodyStr.split(",");
		if(declarationList.size() == 0)
		{
			throw_Wrong_Format_Exception(bodyStr);
		}

		String[] subStrs = new String[declarationList.size()];
		for(int i = 0; i < subStrs.length; i++)
		{
			subStrs[i] = declarationList.get(i).trim();
		}
		return subStrs;
	}

	/**
	 * Gets the _ attribute strs.
	 *
	 * @param declarationStrs the declaration strs
	 *
	 * @return the _ attribute strs
	 */
	public static Vector<String> getAttributesStrs(String[] declarationStrs)
	{
		Vector<String> attrStrs = new Vector<>();
		for(int i = 0; i < declarationStrs.length; i++)
		{
			if(! (declarationStrs[i].toUpperCase().startsWith(
					"CONSTRAINT") || declarationStrs[i].toUpperCase().startsWith(
					"PRIMARY KEY") || declarationStrs[i].toUpperCase().startsWith(
					"INDEX") || declarationStrs[i].toUpperCase().startsWith(
					"KEY") || declarationStrs[i].toUpperCase().startsWith(
					"UNIQUE") || declarationStrs[i].toUpperCase().startsWith(
					"FOREIGN KEY") || declarationStrs[i].toUpperCase().startsWith("CHECK")))
			{
				attrStrs.add(declarationStrs[i]);
				Debug.println("declaration for attribute: " + declarationStrs[i]);
			}
		}
		if(attrStrs.size() == 0)
		{
			throw_Wrong_Format_Exception("");
		}
		return attrStrs;
	}

	/**
	 * Gets the _ constraint strs.
	 *
	 * @param declarationStrs the declaration strs
	 *
	 * @return the _ constraint strs
	 */
	public static Vector<String> getConstraintStrs(String[] declarationStrs)
	{
		Vector<String> constraintStrs = new Vector<>();
		for(int i = 0; i < declarationStrs.length; i++)
		{
			if(declarationStrs[i].toUpperCase().startsWith("PRIMARY KEY") || declarationStrs[i].toUpperCase().contains(
					"FOREIGN KEY") || declarationStrs[i].toUpperCase().contains("CHECK"))
				constraintStrs.add(declarationStrs[i]);

		}
		return constraintStrs;
	}

	/**
	 * Gets the _ data_ field.
	 *
	 * @param tableName the table name
	 * @param attrStr   the attr str
	 * @param position  the position
	 *
	 * @return the _ data_ field
	 */
	public static DataField get_Data_Field(String tableName, String attrStr, int position)
	{
		Debug.println("tableName: " + tableName + " attrStr " + attrStr + " position " + position);
		DataField dF = DataFieldParser.createField(tableName, attrStr, position);
		return dF;
	}

	/**
	 * Gets the _ data_ fields.
	 *
	 * @param tableName     the table name
	 * @param attributeStrs the attribute strs
	 *
	 * @return the _ data_ fields
	 */
	public static LinkedHashMap<String, DataField> getFieldsForTable(String tableName, Vector<String> attributeStrs)
	{
		LinkedHashMap<String, DataField> fieldsMap = new LinkedHashMap<>();
		boolean isContainedLwwDataFields = false;
		for(int i = 0; i < attributeStrs.size(); i++)
		{
			DataField field = CreateStatementParser.get_Data_Field(tableName, attributeStrs.elementAt(i), i);

			if(fieldsMap.containsKey(field.getFieldName()))
			{
				String message = "field " + field.getFieldName() + "is duplicated";
				Runtime.throwRunTimeException(message,ExitCode.HASHMAPDUPLICATE);
			}

			fieldsMap.put(field.getFieldName(), field);
			LOG.trace("field {} from table {} added", field.getFieldName(), field.getTableName());

			if(CrdtFactory.isLwwType(field.getCrdtType()) && ! isContainedLwwDataFields)
				isContainedLwwDataFields = true;

		} if(isContainedLwwDataFields)
	{
		DataField lwwField = DataFieldParser.create_LwwLogicalTimestamp_Data_Field_Instance(tableName,
				attributeStrs.size());

		if(fieldsMap.containsKey(lwwField.getFieldName()))
		{
			String message = "field " + lwwField.getFieldName() + "is duplicated";
			Runtime.throwRunTimeException(message,ExitCode.HASHMAPDUPLICATE);
		}

		fieldsMap.put(lwwField.getFieldName(), lwwField);
		LOG.trace("field {} from table {} added", lwwField.getFieldName(), lwwField.getTableName());
	}
		return fieldsMap;
	}

	/**
	 * Update_ data_ fields.
	 *
	 * @param fieldsMap      the d fs
	 * @param constraintStrs the constraint strs
	 */
	public static void setFieldsConstraints(LinkedHashMap<String, DataField> fieldsMap, Vector<String> constraintStrs)
	{
		for(int i = 0; i < constraintStrs.size(); i++)
		{
			String constraint = constraintStrs.elementAt(i);

			if(constraint.toUpperCase().contains("PRIMARY KEY"))
			{

				int startIndex = constraint.indexOf("(");
				int endIndex = constraint.indexOf(")");
				if(startIndex >= endIndex || startIndex == - 1 || endIndex == - 1)
					throw_Wrong_Format_Exception(constraint);

				String keyStr = constraint.substring(startIndex + 1, endIndex);
				keyStr = keyStr.replaceAll("\\s", "");
				keyStr = keyStr.replaceAll("`", "");
				String[] pKeys = keyStr.split(",");

				for(int j = 0; j < pKeys.length; j++)
				{
					if(! fieldsMap.containsKey(pKeys[j]))
						throw_Wrong_Format_Exception(constraint + " " + pKeys[j]);

					DataField field = fieldsMap.get(pKeys[j]);
					Invariant inv = new UniqueInvariant(field, constraint);
					field.setPrimaryKey();
					field.addInvariant(inv);
				}
			}
			if(constraint.toUpperCase().contains("FOREIGN KEY"))
			{
				int locationIndex = constraint.toUpperCase().indexOf("FOREIGN KEY");
				int startIndex = constraint.indexOf("(", locationIndex);
				int endIndex = constraint.indexOf(")", locationIndex);

				if(startIndex >= endIndex || startIndex == - 1 || endIndex == - 1)
					throw_Wrong_Format_Exception(constraint);

				String keyStr = constraint.substring(startIndex + 1, endIndex);
				keyStr = keyStr.replaceAll("\\s", "");
				keyStr = keyStr.replaceAll("`", "");
				String[] fKeys = keyStr.split(",");

				int locationIndex_2 = constraint.toUpperCase().indexOf("REFERENCES");
				int startIndex_2 = constraint.indexOf(" ", locationIndex_2);
				int endIndex_2 = constraint.indexOf("(", startIndex_2);
				int endIndex_3 = constraint.indexOf(")", endIndex_2);

				String foreignKeyStr = constraint.substring(endIndex_2 + 1, endIndex_3);
				foreignKeyStr = foreignKeyStr.replaceAll("\\s", "");
				foreignKeyStr = foreignKeyStr.replaceAll("`", "");
				String[] foreignAttributes = foreignKeyStr.split(",");

				if(foreignAttributes.length != fKeys.length)
					throw_Wrong_Format_Exception("foreign attributes size do not match");

				String foreignKeyTable = constraint.substring(startIndex_2 + 1, endIndex_2).trim();

				for(int t = 0; t < fKeys.length; t++)
				{
					if(! fieldsMap.containsKey(fKeys[t]))
					{
						throw_Wrong_Format_Exception(constraint + " " + fKeys[t]);
					}
					DataField field = fieldsMap.get(fKeys[t]);
					field.setForeignKey();
					Invariant inv = new ForeignKeyInvariant(field, foreignKeyTable, foreignAttributes[t], constraint);
					field.addInvariant(inv);
				}
			}

			if(constraint.toUpperCase().contains("CHECK"))
			{
				int locationIndex = constraintStrs.elementAt(i).toUpperCase().indexOf("CHECK");
				int startIndex = constraintStrs.elementAt(i).indexOf("(", locationIndex);
				int endIndex = constraintStrs.elementAt(i).indexOf(")", locationIndex);
				if(startIndex >= endIndex || startIndex == - 1 || endIndex == - 1)
				{
					throw_Wrong_Format_Exception(constraintStrs.elementAt(i));
				}

				String conditionStr = constraintStrs.elementAt(i).substring(startIndex + 1, endIndex);

				if(conditionStr.contains("<"))
				{

				} else if(conditionStr.contains(">"))
				{
					String operands[] = conditionStr.split(">");
				} else
					throw_Wrong_Format_Exception(constraintStrs.elementAt(i));

			}
		}
	}

	/**
	 * Gets the _ data_ field_ hash map.
	 *
	 * @param tableName the table name
	 * @param bodyStr   the body str
	 *
	 * @return the _ data_ field_ hash map
	 */
	public static LinkedHashMap<String, DataField> createFields(String tableName, String bodyStr)
	{
		String[] declarations = getDeclarationsStrs(bodyStr);
		Vector<String> attrStrs = getAttributesStrs(declarations);
		Vector<String> consStrs = getConstraintStrs(declarations);

		LinkedHashMap<String, DataField> fieldsMap = getFieldsForTable(tableName, attrStrs);
		setFieldsConstraints(fieldsMap, consStrs);
		return fieldsMap;
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
			throw new RuntimeException("The create table statment " + schemaStr + " is in a wrong format!");
		} catch(RuntimeException e)
		{
			e.printStackTrace();
			System.exit(ExitCode.WRONGCREATTABLEFORMAT);
		}
	}
}
