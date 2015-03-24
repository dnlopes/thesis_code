package network;


import database.invariants.CheckInvariantItem;
import network.node.NodeMetadata;
import org.apache.thrift.TException;
import runtime.operation.ShadowOperation;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork extends INetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, NodeMetadata node);

	public List<CheckInvariantItem> checkInvariants(List<CheckInvariantItem> checkList, NodeMetadata node) throws TException;
}
