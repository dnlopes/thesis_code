package util.zookeeper;


import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

/**
 * Created by dnlopes on 13/07/15.
 */
public interface OperationCoordinationService
{

	public interface CoordinationExtensionOperations
	{
		public static final String OP1 = "/coordination";
	}

	public CoordinatorResponse coordinate(CoordinatorRequest request);

}
