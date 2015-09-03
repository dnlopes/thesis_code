package nodes.replicator;


import nodes.AbstractNetwork;
import nodes.NodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;

import util.thrift.*;
import util.zookeeper.EZKOperationCoordinator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	private Map<Integer, NodeConfig> replicatorsConfigs;
	private EZKOperationCoordinator ezkCoordinator;

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();

		try
		{
			ZooKeeper zooKeeper = new ZooKeeper(Configuration.getInstance().getZookeeperConnectionString(),
					Configuration.ZookeeperDefaults.ZOOKEEPER_SESSION_TIMEOUT, null);
			this.ezkCoordinator = new EZKOperationCoordinator(zooKeeper, this.me.getId());
		} catch(IOException e)
		{
			LOG.error("failed to create zookeeper connection {}: ", e.getMessage(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.NOINITIALIZATION);
		}

		for(NodeConfig replicatorConfig : Configuration.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);
	}

	@Override
	public void sendOperationToRemote(ThriftShadowTransaction thriftOperation)
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
				LOG.warn("failed to send shadow transaction to replicator {}", config.getId(), e);
			} finally
			{
				newTransport.close();
			}
		}
	}

	@Override
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req)
	{
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		return this.ezkCoordinator.coordinate(req);
	}
}
