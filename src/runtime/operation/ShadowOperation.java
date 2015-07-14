package runtime.operation;


import database.util.Row;
import util.thrift.Request;
import util.thrift.ThriftShadowTransaction;

import java.sql.SQLException;


/**
 * Created by dnlopes on 11/05/15.
 */
public interface ShadowOperation
{
	public Row getRow();
	public void generateStatements(ThriftShadowTransaction shadowTransaction);
	public OperationType getOperationType();
	public void createRequestsToCoordinate(Request request) throws SQLException;
	public int getOperationId();
}
