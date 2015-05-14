package runtime.operation;


import database.util.ExecutionPolicy;
import database.util.Row;
import util.thrift.CoordinatorRequest;

import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public interface Operation
{
	public Row getRow();
	public void generateOperationStatements(List<String> shadowStatements);
	public ExecutionPolicy getTablePolicy();
	public OperationType getOperationType();
	public void createRequestsToCoordinate(CoordinatorRequest request);
	public int getOperationId();
}
