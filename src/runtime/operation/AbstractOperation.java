package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public abstract class AbstractOperation implements Operation
{

	protected final ExecutionPolicy tablePolicy;
	protected final OperationType opType;
	protected final Row row;

	public AbstractOperation(ExecutionPolicy tablePolicy, OperationType opType, Row row)
	{
		this.tablePolicy = tablePolicy;
		this.opType = opType;
		this.row = row;
	}

	public ExecutionPolicy getTablePolicy()
	{
		return this.tablePolicy;
	}

	public OperationType getOpType()
	{
		return this.opType;
	}

	public abstract List<String> generateOperationStatements();
}
