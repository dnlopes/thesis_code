package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import util.defaults.DBDefaults;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteOperation extends AbstractOperation implements Operation
{

	private static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";
	private static final String SET_DELETED_CLOCK_EXPRESION = DBDefaults.DELETED_CLOCK_COLUMN + "=" + DBDefaults
			.CONTENT_CLOCK_PLACEHOLDER;
	private static final String FUNCTION_CLAUSE = " AND compareClocks(" + DBDefaults.DELETED_CLOCK_COLUMN + "," +
			DBDefaults.DELETED_CLOCK_PLACEHOLDER + ")";

	public DeleteOperation(int id, ExecutionPolicy policy, Row newRow)
	{
		super(id, policy, OperationType.DELETE, newRow);
	}

	@Override
	public void generateOperationStatements(List<String> shadowStatements)
	{
		this.row.mergeUpdates();

		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(this.row.getTable().getName());
		buffer.append(" SET ");
		buffer.append(SET_DELETED_EXPRESSION);
		buffer.append(",");
		buffer.append(SET_DELETED_CLOCK_EXPRESION);
		buffer.append(" WHERE ");
		buffer.append(row.getPrimaryKeyValue().getPrimaryKeyWhereClause());
		buffer.append(FUNCTION_CLAUSE);
		/*buffer.append(" AND ");
		buffer.append("compareClocks(");
		buffer.append(DBDefaults.DELETED_CLOCK_COLUMN);
		buffer.append(",");
		buffer.append(DBDefaults.CONTENT_CLOCK_PLACEHOLDER);
		buffer.append(")");                                */

		if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			buffer.append(">=0");
		else
			buffer.append(">0");

		shadowStatements.add(buffer.toString());
	}

}
