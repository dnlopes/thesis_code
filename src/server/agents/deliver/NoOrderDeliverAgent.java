package server.agents.deliver;


import common.thrift.CRDTCompiledTransaction;
import server.replicator.Replicator;
import server.util.TransactionCommitFailureException;


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
	public void deliverTransaction(CRDTCompiledTransaction op) throws TransactionCommitFailureException
	{
		this.replicator.commitOperation(op, true);
	}
}
