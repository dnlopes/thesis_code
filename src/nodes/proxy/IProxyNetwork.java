package nodes.proxy;


import nodes.NodeConfig;
import org.apache.thrift.TException;
import runtime.operation.ShadowOperation;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, NodeConfig node);

	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req, NodeConfig node) throws TException;
}
