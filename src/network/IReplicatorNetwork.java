package network;


import network.node.AbstractNode;
import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IReplicatorNetwork extends INetwork
{
	public void sendOperationAsync(ShadowOperation shadowOp, AbstractNode node);
}
