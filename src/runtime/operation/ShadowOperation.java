package runtime.operation;


import database.util.Row;
import util.thrift.CoordinatorRequest;
import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 11/05/15.
 */
public interface ShadowOperation
{
	public Row getRow();
	public void generateStatements(ThriftShadowTransaction shadowTransaction);
	public OperationType getOperationType();
	public void createRequestsToCoordinate(CoordinatorRequest request);
	public int getOperationId();
}
