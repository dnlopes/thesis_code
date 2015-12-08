package server.agents.dispatcher;


import common.thrift.CRDTPreCompiledTransaction;
import server.replicator.IReplicatorNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.replicator.Replicator;
import server.util.TransactionCommitFailureException;


/**
 * Created by dnlopes on 08/10/15.
 * Basic dispatcher immediately forwards incoming transactions to remote replicators.
 */
public class BasicDispatcher implements DispatcherAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(BasicDispatcher.class);

	private final IReplicatorNetwork networkInterface;

	public BasicDispatcher(Replicator replicator)
	{
		this.networkInterface = replicator.getNetworkInterface();
	}

	@Override
	public void dispatchTransaction(CRDTPreCompiledTransaction transaction) throws TransactionCommitFailureException
	{
		if(!transaction.isReadyToCommit())
			throw new TransactionCommitFailureException("transaction is not ready for commit");

		networkInterface.sendOperationToRemote(transaction);
	}
}
