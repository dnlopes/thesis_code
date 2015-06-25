package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.transformer.OperationTransformer;
import util.defaults.DBDefaults;
import util.thrift.ThriftShadowTransaction;

/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteOperation extends AbstractOperation implements ShadowOperation
{
	protected static final String DELETE_WINS_FUNCTION_CLAUSE = DBDefaults.CLOCKS_IS_CONCURRENT_OR_GREATER_FUNCTION + "" +
			"(" + DBDefaults.CONTENT_CLOCK_COLUMN + "," + DBDefaults.CLOCK_VALUE_PLACEHOLDER + ")=TRUE";
	protected static final String UPDATE_WINS_FUNCTION_CLAUSE = DBDefaults.IS_STRICTLY_GREATER_FUNCTION + "" +
			"(" + DBDefaults.CONTENT_CLOCK_COLUMN + "," + DBDefaults.CLOCK_VALUE_PLACEHOLDER + ")=TRUE";
	protected static final String CLOCK_IS_GREATER_FUNCTION_CLAUSE = DBDefaults.CLOCK_IS_GREATER_FUNCTION + "" +
			"(" + DBDefaults.DELETED_CLOCK_COLUMN + "," + DBDefaults.CLOCK_VALUE_PLACEHOLDER + ")=TRUE";

	protected static final String SET_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=1";


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

		if(this.tablePolicy == ExecutionPolicy.DELETEWINS)
			buffer.append(DELETE_WINS_FUNCTION_CLAUSE);
		else
			buffer.append(UPDATE_WINS_FUNCTION_CLAUSE);

		buffer.append(" AND ");
		buffer.append(CLOCK_IS_GREATER_FUNCTION_CLAUSE);

		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), buffer.toString());
	}

}
