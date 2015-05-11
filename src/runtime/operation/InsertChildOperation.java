package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertChildOperation extends InsertOperation
{

	private Row[] parentRows;

	public InsertChildOperation(ExecutionPolicy policy, Row[] parents, Row newRow)
	{
		super(policy, newRow);
		this.parentRows = parents;
	}

	@Override
	public String[] generateOperationStatements()
	{
		//TODO implement
		return null;
	}
}
