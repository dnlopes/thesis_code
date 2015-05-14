package network.proxy;


import network.AbstractNodeConfig;
import org.apache.thrift.TException;
import runtime.operation.ShadowOperation;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, AbstractNodeConfig node);

	public CoordinatorResponse checkInvariants(CoordinatorRequest req, AbstractNodeConfig node) throws TException;
}
