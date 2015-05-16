package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.OperationTransformer;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteOperation extends AbstractOperation implements Operation
{

	public DeleteOperation(int id, ExecutionPolicy policy, Row newRow)
	{
		super(id, policy, OperationType.DELETE, newRow);
	}

	@Override
	public void generateOperationStatements(List<String> shadowStatements)
	{
		StringBuilder buffer = new StringBuilder();

		String deleteStatement = OperationTransformer.generateDeleteStatement(this.row);

		buffer.append(deleteStatement);
		buffer.append(" AND ");
		String clockClause = OperationTransformer.generateVisibilityUpdateFunctionClause(this.tablePolicy);
		buffer.append(clockClause);

		if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			buffer.append(">=0");
		else
			buffer.append(">0");

		shadowStatements.add(buffer.toString());
	}

}
