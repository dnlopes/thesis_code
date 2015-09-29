package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CoordinatorRequest;
import util.thrift.ThriftShadowTransaction;

import java.sql.SQLException;


/**
 * Created by dnlopes on 13/05/15.
 */
public abstract class AbstractOperation implements ShadowOperation
{
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractOperation.class);

	protected boolean isFinal;
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
		this.isFinal = true;
	}

	public Row getRow()
	{
		return this.row;
	}

	public int getOperationId()
	{
		return this.id;
	}

	public OperationType getOperationType()
	{
		return this.opType;
	}

	public abstract void generateStatements(ThriftShadowTransaction shadowTransaction);

	@Override
	public void createRequestsToCoordinate(CoordinatorRequest request) throws SQLException
	{
		// do nothing
		// if one wants to coordinate, then override this method
	}

	public boolean isFinal()
	{
		return this.isFinal;
	}
}
