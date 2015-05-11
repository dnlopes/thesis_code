package runtime.operation;


import database.constraints.fk.ForeignKeyPolicy;
import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public class InsertOperation implements Operation
{

	private ExecutionPolicy tablePolicy;
	private Row newRow;

	public InsertOperation(ExecutionPolicy policy, Row newRow)
	{
		this.tablePolicy = policy;
		this.newRow = newRow;
	}

	public String[] generateOperationStatements()
	{
		//TODO implement
		return null;
	}
}
