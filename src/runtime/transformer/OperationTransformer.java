package runtime.transformer;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.value.FieldValue;
import database.util.Row;
import org.apache.zookeeper.server.util.Profiler;
import runtime.operation.OperationsStatements;
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
	private static final String SET_DELETED_CLOCK_EXPRESION = DBDefaults.DELETED_CLOCK_COLUMN + "=" + DBDefaults
			.CLOCK_VALUE_PLACEHOLDER;

	public static String generateInsertStatement(Row row)
	{
		//done
		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder();

		buffer.append(OperationsStatements.INSERT_INTO);
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

		buffer.append(OperationsStatements.PARENT_VALUES_PARENT);
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

			//@info dont update primary keys because they are immutable
			if(fValue.getDataField().isPrimaryKey())
				continue;

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

	public static String generateContentUpdateFunctionClause(boolean equal)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(DBDefaults.CLOCK_IS_GREATER_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")=1");

		return buffer.toString();
	}

	public static String generateContentUpdateFunctionClause(boolean equal, String firstClock, String secondClock)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(DBDefaults.COMPARE_CLOCK_FUNCTION);
		buffer.append("(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")");

		if(equal)
			buffer.append(" >= 0");
		else
			buffer.append(" > 0");

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

		String subQuery = QueryCreator.createFindChildQuery(parentRow, fkConstraint.getChildTable(),
				fkConstraint.getFieldsRelations());

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

	/**
	 * Generates a SQL statement that sets the visibility flag to TRUE
	 * It does so silenty, which means that this statement will leave no footprint. In other words, no one will know
	 * that this statement was executed.
	 *
	 * @param row
	 *
	 * @return
	 */
	public static String generateSetVisible(Row row)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_NOT_DELETED_EXPRESSION);
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
	public static String generateUpdateChildForeignKeyFields()
	{
		return null;
	}

	public static String generateSetParentVisible(ForeignKeyConstraint constraint, Row parent)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append(OperationsStatements.UPDATE);
		buffer.append(constraint.getParentTable().getName());
		buffer.append(OperationsStatements.SET_DELETED);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(parent.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.VISIBLE_PARENT_OP_SUFFIX);

		return buffer.toString();
	}

	public static String mergeDeletedClock(Row parent)
	{

		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationsStatements.UPDATE);
		buffer.append(parent.getTable().getName());
		buffer.append(OperationsStatements.MERGE_DCLOCK_OP);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(parent.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}
}
