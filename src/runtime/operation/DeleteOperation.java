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
		//done
		String deleteStatement;

		if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			deleteStatement = OperationTransformer.generateDeleteDeleteWins(this.row);
		else
			deleteStatement = OperationTransformer.generateDeleteUpdateWins(this.row);

		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), deleteStatement);
	}

}
