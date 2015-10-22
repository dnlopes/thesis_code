package util.zookeeper;


import org.apache.zookeeper.KeeperException;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

/**
 * Created by dnlopes on 13/07/15.
 */
public interface EZKCoordinationService
{

	void init(String codeBasePath) throws KeeperException, InterruptedException;
	void closeExtension() throws InterruptedException;

	void cleanupDatabase() throws KeeperException, InterruptedException;
	CoordinatorResponse sendRequest(CoordinatorRequest request);

}
