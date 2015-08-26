package nodes.replicator;

import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.Request;
import util.thrift.ThriftShadowTransaction;


/**
 * @author dnlopes
 *         This interface defines methods for all Replicator communications.
 *         All methods should be thread-safe, because this class is used by multiple threads to send events to
 *         remote nodes
 */
public interface IReplicatorNetwork
{
	public void sendOperationToRemote(ThriftShadowTransaction thriftOperation);
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req);
}
