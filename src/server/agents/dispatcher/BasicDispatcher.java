package server.agents.dispatcher;


import server.replicator.IReplicatorNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.CRDTCompiledTransaction;
import common.thrift.CRDTTransaction;
import common.thrift.ThriftUtils;
import server.replicator.Replicator;


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
	public void dispatchTransaction(CRDTTransaction transaction)
	{
		CRDTCompiledTransaction compiledTransaction;

		if(transaction.isSetCompiledTxn())
			compiledTransaction = transaction.getCompiledTxn();
		else
			compiledTransaction = ThriftUtils.compileCRDTTransaction(transaction);

		if(compiledTransaction != null)
			this.networkInterface.sendOperationToRemote(compiledTransaction);
		else
			LOG.warn("failed to dispatch txn: compilation process failed");
	}
}
