package util.zookeeper;


import org.apache.zookeeper.KeeperException;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

/**
 * Created by dnlopes on 13/07/15.
 */
public interface EZKCoordinationService
{

	public void init(String codeBasePath) throws KeeperException, InterruptedException;
	public void closeExtension() throws InterruptedException;

	public void cleanupDatabase() throws KeeperException, InterruptedException;
	public CoordinatorResponse coordinate(CoordinatorRequest request);

}
