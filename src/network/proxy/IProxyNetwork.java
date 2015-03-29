package network.proxy;


import network.AbstractConfig;
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
	public boolean commitOperation(ShadowOperation shadowOp, AbstractConfig node);

	public CoordResponseMessage checkInvariants(List<RequestEntry> checkList, AbstractConfig node) throws TException;
}
