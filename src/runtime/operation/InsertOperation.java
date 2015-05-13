package runtime.operation;

import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertOperation extends AbstractOperation implements Operation
{

	public InsertOperation(ExecutionPolicy policy, Row newRow)
	{
		super(policy, OperationType.INSERT, newRow);
	}

	public List<String> generateOperationStatements()
	{
		//TODO implement
		return null;
	}
}
