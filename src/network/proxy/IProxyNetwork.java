package network.proxy;


import network.NodeMetadata;
import org.apache.thrift.TException;
import runtime.operation.ShadowOperation;
import util.thrift.ThriftCheckEntry;
import util.thrift.ThriftCheckResponse;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, NodeMetadata node);

	public ThriftCheckResponse checkInvariants(List<ThriftCheckEntry> checkList, NodeMetadata node) throws TException;
}
