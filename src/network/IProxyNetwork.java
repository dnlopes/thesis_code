package network;


import network.node.AbstractNode;
import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork extends INetwork
{
	public boolean commitOperation(ShadowOperation shadowOp, AbstractNode node);
}
