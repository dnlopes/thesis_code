package network.replicator;


import network.AbstractNetwork;
import network.AbstractNodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);
	private boolean alreadyInitialized;

	public ReplicatorNetwork(AbstractNodeConfig node)
	{
		super(node);
		this.alreadyInitialized = false;
	}

	@Override
	public void sendOperationAsync(ThriftOperation thriftOperation)
	{
		if(!alreadyInitialized)
			this.bindWithRemoteReplicators();

		for(ReplicatorRPC.Client client : this.replicatorsClients.values())
			try
			{
				client.commitOperationAsync(thriftOperation);
			} catch(TException ex)
			{
				LOG.warn("failed to send async op to replicator");
			}
	}

	private void bindWithRemoteReplicators()
	{
		for(AbstractNodeConfig config : Configuration.getInstance().getAllReplicatorsConfig().values())
			try
			{
				this.addNode(config);
			} catch(TTransportException e)
			{
				LOG.error("failed to bind with {}", config.getName());
			}
		this.alreadyInitialized = true;
	}
}
