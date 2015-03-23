package network;


import network.node.AbstractNode;
import network.node.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;

/**
 * Created by dnlopes on 21/03/15.
 */
public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	public ReplicatorNetwork(AbstractNode node)
	{
		super(node);
	}

	@Override
	public void sendOperationAsync(ShadowOperation shadowOp, NodeMetadata node)
	{
		//TODO
	}
}
