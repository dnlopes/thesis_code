package network.replicator;


import network.AbstractConfig;
import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IReplicatorNetwork
{
	public void sendOperationAsync(ShadowOperation shadowOp, AbstractConfig node);
}