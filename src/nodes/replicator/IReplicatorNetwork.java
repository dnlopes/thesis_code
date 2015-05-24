package nodes.replicator;

import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IReplicatorNetwork
{
	public void sendOperationToRemote(ThriftOperation thriftOperation);
}
