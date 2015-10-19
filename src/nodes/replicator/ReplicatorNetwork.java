package nodes.replicator;


import nodes.AbstractNetwork;
import nodes.NodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Configuration;

import util.defaults.ZookeeperDefaults;
import util.thrift.*;
import util.zookeeper.EZKCoordinationClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	private Map<Integer, NodeConfig> replicatorsConfigs;
	private EZKCoordinationClient ezkClient;

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();

		ZooKeeper zooKeeper = null;
		try
		{
			zooKeeper = new ZooKeeper(Configuration.getInstance().getZookeeperConnectionString(),
					ZookeeperDefaults.ZOOKEEPER_SESSION_TIMEOUT, null);
		} catch(IOException e)
		{
			LOG.error("failed to create zookeeper connection {}: ", e.getMessage(), e);
		}

		this.ezkClient = new EZKCoordinationClient(zooKeeper, this.me.getId());

		try
		{
			this.ezkClient.init(Configuration.getInstance().getExtensionCodeDir());
		} catch(KeeperException | InterruptedException e)
		{
			LOG.error("failed to install zookeeper extension {}: ", e.getMessage(), e);
		}

		for(NodeConfig replicatorConfig : Configuration.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);
	}

	@Override
	public void sendOperationToRemote(CRDTCompiledTransaction transaction)
	{
		for(NodeConfig config : this.replicatorsConfigs.values())
		{
			TTransport newTransport = new TSocket(config.getHost(), config.getPort());

			try
			{
				newTransport.open();
				TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
				ReplicatorRPC.Client client = new ReplicatorRPC.Client(protocol);
				client.commitOperationAsync(transaction);
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

		return this.ezkClient.coordinate(req);
	}
}
