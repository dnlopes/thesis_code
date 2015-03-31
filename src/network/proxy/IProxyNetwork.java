package network.proxy;


import network.AbstractNodeConfig;
import org.apache.thrift.TException;
import runtime.operation.ShadowOperation;
import util.thrift.CoordResponseMessage;
import util.thrift.RequestEntry;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, AbstractNodeConfig node);

	public CoordResponseMessage checkInvariants(List<RequestEntry> checkList, AbstractNodeConfig node) throws TException;
}
