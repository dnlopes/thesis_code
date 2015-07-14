package util.zookeeper;


import org.apache.zookeeper.KeeperException;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

/**
 * Created by dnlopes on 13/07/15.
 */
public interface OperationCoordinationService
{

	public CoordinatorResponse coordinate(CoordinatorRequest request);
	public void init(String codeBasePath) throws Exception;
	public void cleanup() throws KeeperException, InterruptedException;

}
