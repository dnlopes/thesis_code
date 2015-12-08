package server.agents.deliver;


import common.thrift.CRDTPreCompiledTransaction;
import server.replicator.Replicator;
import common.thrift.CRDTCompiledTransaction;


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
	public void deliverTransaction(CRDTPreCompiledTransaction op)
	{
		this.replicator.deliverTransaction(op);
	}
}
