package nodes.replicator;


import nodes.Deliver;
import runtime.operation.ShadowOperation;


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
	public void dispatchOperation(ShadowOperation op)
	{
		this.replicator.deliverShadowOperation(op);
	}
}
