package network.replicator;


import network.AbstractNetwork;
import network.AbstractConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	public ReplicatorNetwork(ReplicatorConfig node)
	{
		super(node);
	}

	@Override
	public void sendOperationAsync(ShadowOperation shadowOp, AbstractConfig node)
	{
		//TODO
	}
}
