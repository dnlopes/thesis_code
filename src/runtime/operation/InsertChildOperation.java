package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertChildOperation extends InsertOperation
{

	private List<Row> parentRows;

	public InsertChildOperation(ExecutionPolicy policy, List<Row> parents, Row newRow)
	{
		super(policy, newRow);
		this.parentRows = parents;
	}

	@Override
	public List<String> generateOperationStatements()
	{
		//TODO implement
		return null;
	}
}
