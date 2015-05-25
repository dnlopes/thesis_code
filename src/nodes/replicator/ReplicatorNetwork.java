package nodes.replicator;


import nodes.AbstractNetwork;
import nodes.NodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);
	private Map<Integer, NodeConfig> replicatorsConfigs;

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();

		for(NodeConfig replicatorConfig : Configuration.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);
	}

	@Override
	public void sendOperationToRemote(ThriftOperation thriftOperation)
	{
		for(NodeConfig config : this.replicatorsConfigs.values())
		{
			TTransport newTransport = new TSocket(config.getHost(), config.getPort());

			try
			{
				newTransport.open();
				TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
				ReplicatorRPC.Client client = new ReplicatorRPC.Client(protocol);
				client.commitOperationAsync(thriftOperation);
			} catch(TException e)
			{
				LOG.warn("failed to send shadow operation to replicator {}", config.getId(), e);
			} finally
			{
				newTransport.close();
			}
		}
	}
}
