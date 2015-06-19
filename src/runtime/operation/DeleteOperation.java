package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.transformer.OperationTransformer;
import util.thrift.ThriftShadowTransaction;

/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteOperation extends AbstractOperation implements ShadowOperation
{

	public DeleteOperation(int id, ExecutionPolicy policy, Row row)
	{
		super(id, policy, OperationType.DELETE, row);
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
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

		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), buffer.toString());
	}

}
