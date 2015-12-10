package server.agents.dispatcher;


import common.thrift.CRDTCompiledTransaction;
import server.replicator.IReplicatorNetwork;
import server.replicator.Replicator;


/**
 * Created by dnlopes on 08/10/15.
 * Basic dispatcher immediately forwards incoming transactions to remote replicators.
 */
public class BasicDispatcher implements DispatcherAgent
{

	private final IReplicatorNetwork networkInterface;

	public BasicDispatcher(Replicator replicator)
	{
		this.networkInterface = replicator.getNetworkInterface();
	}

	@Override
	public void dispatchTransaction(CRDTCompiledTransaction transaction)
	{
		networkInterface.sendOperationToRemote(transaction);
	}
}
