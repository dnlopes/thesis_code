package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.List;


/**
 * Created by dnlopes on 12/05/15.
 */
public class UpdateOperation extends AbstractOperation implements Operation
{

	public UpdateOperation(ExecutionPolicy policy, Row updatedRow)
	{
		super(policy, OperationType.UPDATE, updatedRow);
	}

	public List<String> generateOperationStatements()
	{
		//TODO implement
		return null;
	}
}
