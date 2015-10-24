package nodes.replicator.deliver;


import nodes.replicator.Replicator;
import util.thrift.CRDTCompiledTransaction;


/**
 * Created by dnlopes on 24/05/15.
 */
public class NoOrderDeliverAgent implements DeliverAgent
{

	private final Replicator replicator;

	public NoOrderDeliverAgent(Replicator replicator)
	{
		this.replicator = replicator;
	}

	@Override
	public void deliverTransaction(CRDTCompiledTransaction op)
	{
		this.replicator.deliverTransaction(op);
	}
}
