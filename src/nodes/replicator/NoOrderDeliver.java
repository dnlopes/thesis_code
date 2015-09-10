package nodes.replicator;


import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 24/05/15.
 */
public class NoOrderDeliver implements DeliverAgent
{

	private final Replicator replicator;

	public NoOrderDeliver(Replicator replicator)
	{
		this.replicator = replicator;
	}

	@Override
	public void dispatchOperation(ThriftShadowTransaction op)
	{
		this.replicator.deliverShadowTransaction(op);
	}
}
