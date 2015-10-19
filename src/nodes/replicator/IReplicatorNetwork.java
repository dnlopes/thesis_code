package nodes.replicator;

import util.thrift.CRDTCompiledTransaction;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.ThriftShadowTransaction;


/**
 * @author dnlopes
 *         This interface defines methods for all Replicator communications.
 *         All methods should be thread-safe, because this class is used by multiple threads to send events to
 *         remote nodes
 */
public interface IReplicatorNetwork
{
	void sendOperationToRemote(CRDTCompiledTransaction transaction);
	CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req);
}
