package runtime.transformer;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.value.FieldValue;
import database.util.Row;
import runtime.operation.OperationsStatements;
import util.defaults.DatabaseDefaults;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author dnlopes
 *         This class generates SQL shadow operations
 *         It is used to build the shadow transaction
 */
public class OperationTransformer
{

	private static final String SET_DELETED_EXPRESSION = DatabaseDefaults.DELETED_COLUMN + "=1";
	private static final String SET_DELETED_CLOCK_EXPRESION = DatabaseDefaults.DELETED_CLOCK_COLUMN + "=" +
			DatabaseDefaults.CLOCK_VALUE_PLACEHOLDER;

	/**
	 * Generates a SQL insert statement to a table that has no forein key defined
	 *
	 * @param row
	 *
	 * @return
	 */
	public static String generateInsertStatement(Row row)
	{
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

		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.SET);

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

		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.CLOCK_IS_GREATER_SUFIX);

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

	public static String generateSetParentVisible(ForeignKeyConstraint constraint, Row parent)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append(OperationsStatements.UPDATE);
		buffer.append(constraint.getParentTable().getName());
		buffer.append(OperationsStatements.SET_NOT_DELETED);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(parent.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.VISIBLE_PARENT_OP_SUFFIX);

		return buffer.toString();
	}

	public static String mergeDeletedClock(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.MERGE_DCLOCK_OP);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	public static String mergeContentClock(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.MERGE_CCLOCK_OP);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

	/**
	 * Generates a SQL statement that sets the deleted flag to FALSE for this @childRow
	 * This operation is executed together with @UPDATEWINS policy
	 * to allow rows to be 'visible' again in case some concurrent delete operation deleted it
	 *
	 * @param childRow
	 *
	 * @return
	 */
	public static String generateInsertRowBack(Row childRow)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationTransformer.generateSetVisible(childRow));
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.IS_CONCURRENT_OR_GREATER_DCLOCK);

		return buffer.toString();
	}

	public static String generateConditionalSetChildVisibleOnUpdate(Row childRow,
																	Map<ForeignKeyConstraint, Row> parentRows)
	{
		StringBuilder buffer = new StringBuilder();

		String parentsCounterQuery = QueryCreator.countParentsVisible(parentRows);
		buffer.append(OperationTransformer.generateSetVisible(childRow));
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.IS_CONCURRENT_OR_GREATER_DCLOCK);
		buffer.append(OperationsStatements.AND);
		buffer.append(parentRows.size());
		buffer.append("=(");
		buffer.append(parentsCounterQuery);
		buffer.append(")");

		return buffer.toString();
	}

	/**
	 * Update the deleted flag of a children row depending on the visibility of its parents
	 * If all parents of this child are visible (i.e. not deleted), then the child deleted flag is set to 0
	 *
	 * @param childRow
	 * @param parentRows
	 *
	 * @return
	 */
	public static String generateConditionalSetChildVisibleOnInsert(Row childRow,
																	Map<ForeignKeyConstraint, Row> parentRows)
	{
		StringBuilder buffer = new StringBuilder();

		String parentsCounterQuery = QueryCreator.countParentsVisible(parentRows);
		String visibleOp = OperationTransformer.generateSetVisible(childRow);
		buffer.append(visibleOp);
		buffer.append(OperationsStatements.AND);
		buffer.append(parentRows.size());
		buffer.append("=(");
		buffer.append(parentsCounterQuery);
		buffer.append(")");

		return buffer.toString();
	}

	//****************************************************************************************//
	//********************************* DELETE OPs
	//****************************************************************************************//

	public static String generateDeleteUpdateWins(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.SET_DELETED);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.DELETE_ROW_OP_SUFFIX_UPDATE_WINS);

		return buffer.toString();
	}

	public static String generateDeleteDeleteWins(Row row)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.SET_DELETED);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(OperationsStatements.AND);
		buffer.append(OperationsStatements.DELETE_ROW_OP_SUFFIX_DELETE_WINS);

		return buffer.toString();
	}

	//****************************************************************************************//
	//********************************* AUXILIAR METHODS
	//****************************************************************************************//

	private static String generateSetVisible(Row row)
	{
		StringBuilder buffer = new StringBuilder();

		buffer.append(OperationsStatements.UPDATE);
		buffer.append(row.getTable().getName());
		buffer.append(OperationsStatements.SET_NOT_DELETED);
		buffer.append(OperationsStatements.WHERE);
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());

		return buffer.toString();
	}

}
