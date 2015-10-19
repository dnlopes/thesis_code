package nodes.replicator.dispatcher;


import nodes.replicator.IReplicatorNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CRDTCompiledTransaction;
import util.thrift.CRDTTransaction;
import util.thrift.ThriftUtils;


/**
 * Created by dnlopes on 08/10/15.
 */
public class BasicDispatcher implements DispatcherAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(BasicDispatcher.class);

	private final IReplicatorNetwork networkInterface;

	public BasicDispatcher(IReplicatorNetwork networkInterface)
	{
		this.networkInterface = networkInterface;
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
