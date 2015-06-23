package runtime.transformer;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.field.DataField;
import database.util.ExecutionPolicy;
import database.util.FieldValue;
import database.util.Row;
import util.defaults.DBDefaults;

import java.util.Iterator;
import java.util.List;


/**
 * @author dnlopes
 *         This class generates SQL shadow operations
 *         It is used to build the shadow transaction
 */
public class OperationTransformer
{

	private static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";
	private static final String SET_NOT_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=0";
	private static final String SET_DELETED_CLOCK_NULL = DBDefaults.DELETED_CLOCK_COLUMN+ "=NULL";
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

	public static String generateContentUpdateFunctionClause()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(") >= 0");

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

		//@info, this piece of code prevents state divergence in the case where 2 replicas concurrently deletes the
		// same tuple. Without this code, the value of '_dclock' would not converge
		buffer.append(" AND ");
		buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")");
		buffer.append(">0");

		return buffer.toString();
	}

	/**
	 * Generates a SQL statement that makes sure that the parent row is re-inserted back.
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

		/*
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
		}   */

		buffer.append(" WHERE ");
		buffer.append(parentRow.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	public static String generateDeleteChilds(ForeignKeyConstraint fkConstraint, List<Row> childs)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("UPDATE ");
		buffer.append(fkConstraint.getChildTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(fkConstraint.getChildTable().getPrimaryKey().getQueryClause());
		buffer.append(" IN (");

		Iterator<Row> childsIterator = childs.iterator();

		while(childsIterator.hasNext())
		{
			buffer.append("(");
			buffer.append(childsIterator.next().getPrimaryKeyValue().getValue());
			buffer.append(")");
			if(childsIterator.hasNext())
				buffer.append(",");
		}

		buffer.append(")");

		return buffer.toString();
	}

	public static String generateDeleteConcurrentChilds(Row parentRow, ForeignKeyConstraint fkConstraint)
	{

		String subQuery = QueryCreator.createFindChildNestedQuery(parentRow, fkConstraint
				.getChildTable(), fkConstraint.getFieldsRelations());

		StringBuilder buffer = new StringBuilder();

		buffer.append("UPDATE ");
		buffer.append(fkConstraint.getChildTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(fkConstraint.getChildTable().getPrimaryKey().getQueryClause());
		buffer.append(" = (");
		buffer.append(subQuery);
		buffer.append(")");

		return buffer.toString();
	}

	public static String generateSetVisible(Row row)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_NOT_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_NULL);
		buffer.append(" WHERE ");
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	/**
	 * Generates a SQL statement that update the foreign key fields in childs row according to the parent
	 * It does so silenty, which means that this statement will leave no footprint. In other words, no one will know
	 * that this statement was executed.
	 *
	 * @param parentRow
	 *
	 * @return
	 */
	public static String generateUpdateChildFields()
	{
		return null;
	}
}
