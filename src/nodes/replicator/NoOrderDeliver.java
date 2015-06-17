package nodes.replicator;


import nodes.Deliver;
import runtime.operation.ShadowTransaction;


/**
 * Created by dnlopes on 24/05/15.
 */
public class NoOrderDeliver implements Deliver
{

	private final Replicator replicator;

	public NoOrderDeliver(Replicator replicator)
	{
		this.replicator = replicator;
	}

	@Override
	public void dispatchOperation(ShadowTransaction op)
	{
		this.replicator.deliverShadowTransaction(op);
	}
}
