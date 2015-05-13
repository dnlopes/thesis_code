package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import util.defaults.DBDefaults;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteOperation extends AbstractOperation implements Operation
{

	private static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";
	private static final String SET_DELETED_CLOCK_EXPRESION = DBDefaults.DELETED_CLOCK_COLUMN + "=" + DBDefaults
			.CLOCK_VALUE_PLACEHOLDER;

	public DeleteOperation(ExecutionPolicy policy, Row newRow)
	{
		super(policy, OperationType.DELETE, newRow);
	}

	public List<String> generateOperationStatements()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(row.getPkValue().getPrimaryKeyWhereClause());
		buffer.append(" AND ");
		buffer.append("compareClocks(");
		buffer.append(DBDefaults.CONTENT_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CLOCK_VALUE_PLACEHOLDER);
		buffer.append(")");

		if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			buffer.append(">= 0");
		else
			buffer.append("> 0");

		List<String> transformedOps = new ArrayList<>(1);
		transformedOps.add(buffer.toString());
		return transformedOps;
	}
}
