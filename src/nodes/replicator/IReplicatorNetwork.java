package nodes.replicator;

import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;
import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IReplicatorNetwork
{
	public void sendOperationToRemote(ThriftShadowTransaction thriftOperation);
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req);
	public void releaseResources();
}
