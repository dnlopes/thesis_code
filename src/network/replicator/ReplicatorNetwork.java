package network.replicator;


import network.AbstractNetwork;
import network.AbstractNodeConfig;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	public ReplicatorNetwork(AbstractNodeConfig node)
	{
		super(node);
	}

	@Override
	public void sendOperationAsync(ThriftOperation thriftOperation)
	{
		for(ReplicatorRPC.Client client : this.replicatorsClients.values())
			try
			{
				client.commitOperationAsync(thriftOperation);
			} catch(TException ex)
			{
				LOG.warn("failed to send async op to replicator");
			}
	}
}
