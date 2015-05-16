package runtime;


import database.util.*;
import util.defaults.DBDefaults;

import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 06/05/15.
 */
public class OperationTransformer
{

	private static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";
	private static final String SET_DELETED_CLOCK_EXPRESION = DBDefaults.DELETED_CLOCK_COLUMN + "=" + DBDefaults
			.CONTENT_CLOCK_PLACEHOLDER;

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
		StringBuilder valuesBuffer = new StringBuilder();

		buffer.append("INSERT INTO ");
		buffer.append(row.getTable().getName());
		buffer.append(" (");

		Iterator<FieldValue> fieldsValuesIt = row.getFieldValues().iterator();

		while(fieldsValuesIt.hasNext())
		{
			FieldValue fValue = fieldsValuesIt.next();
			buffer.append(fValue.getDataField().getFieldName());

			if(fValue.getDataField().isDeltaField())
				valuesBuffer.append(fValue.getValue());
			else
				valuesBuffer.append(fValue.getFormattedValue());

			if(fieldsValuesIt.hasNext())
			{
				buffer.append(",");
				valuesBuffer.append(",");
			}
		}

		buffer.append(") VALUES (");
		buffer.append(valuesBuffer.toString());
		buffer.append(") ON DUPLICATE KEY UPDATE ");

		fieldsValuesIt = row.getFieldValues().iterator();
		while(fieldsValuesIt.hasNext())
		{
			FieldValue fValue = fieldsValuesIt.next();
			buffer.append(fValue.getDataField().getFieldName());

			if(fValue.getDataField().isDeltaField())
			{
				buffer.append("=");
				buffer.append(fValue.getFormattedValue());
			} else
			{
				buffer.append("=VALUES(");
				buffer.append(fValue.getDataField().getFieldName());
				buffer.append(")");
			}

			if(fieldsValuesIt.hasNext())
				buffer.append(",");
		}

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

	private static String getPkValuesList(List<PrimaryKeyValue> pkValues)
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<PrimaryKeyValue> pkIt = pkValues.iterator();
		while(pkIt.hasNext())
		{
			buffer.append("(");
			buffer.append(pkIt.next().getValue());
			buffer.append(")");
			if(pkIt.hasNext())
				buffer.append(",");
		}

		return buffer.toString();
	}

}
