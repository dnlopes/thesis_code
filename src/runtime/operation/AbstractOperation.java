package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.DBDefaults;
import util.thrift.CoordinatorRequest;

import java.util.List;


/**
 * Created by dnlopes on 13/05/15.
 */
public abstract class AbstractOperation implements Operation
{

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractOperation.class);
	protected static final String SET_NOT_DELETED_EXPRESSION = DBDefaults.DELETED_COLUMN + "=0";


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

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request)
	{
		// do nothing
		// if one wants to coordinate, then override this method
	}
}
