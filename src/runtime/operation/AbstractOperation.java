package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public abstract class AbstractOperation implements Operation
{

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractOperation.class);

	protected final ExecutionPolicy tablePolicy;
	protected final OperationType opType;
	protected final int id;
	protected Row row;

	public AbstractOperation(int id, ExecutionPolicy tablePolicy, OperationType opType, Row row)
	{
		this.id = id;
		this.tablePolicy = tablePolicy;
		this.opType = opType;
		this.row = row;
	}

	public Row getRow()
	{
		return this.row;
	}

	public int getOperationId()
	{
		return this.id;
	}

	public ExecutionPolicy getTablePolicy()
	{
		return this.tablePolicy;
	}

	public OperationType getOperationType()
	{
		return this.opType;
	}

	public abstract void generateOperationStatements(List<String> shadowStatements);

	public void createRequestsToCoordinate()
	{
		RuntimeHelper.throwRunTimeException("this method should always be overrided if one wants to call it",
				ExitCode.UNEXPECTED_OP);
	}
}
