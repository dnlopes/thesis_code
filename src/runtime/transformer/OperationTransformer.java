package runtime.transformer;


import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.fk.ParentChildRelation;
import database.util.field.DataField;
import database.util.ExecutionPolicy;
import database.util.FieldValue;
import database.util.Row;
import util.defaults.DBDefaults;

import java.util.Iterator;


/**
 * @author dnlopes
 *         This class generates SQL shadow operations
 *         It is used to build the shadow transaction
 */
public class OperationTransformer
{

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

}
