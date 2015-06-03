package nodes.replicator;


import nodes.AbstractNetwork;
import nodes.NodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ObjectPool;
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

	private static final int POOL_SIZE = 100;
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);
	private Map<Integer, NodeConfig> replicatorsConfigs;
	private Map<Integer, ObjectPool<ReplicatorRPC.Client>> rpcsObjects;

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();
		this.rpcsObjects = new HashMap<>();

		for(NodeConfig replicatorConfig : Configuration.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);

		this.setup();
	}

	private void setup()
	{
		for(NodeConfig config : this.replicatorsConfigs.values())
			this.createConnectionPoolForReplicator(config);
	}

	private void createConnectionPoolForReplicator(NodeConfig config)
	{
		LOG.trace("creating connection pool to replicator {}", config.getId());

		boolean isReady = false;
		do
		{
			TTransport newTransport = new TSocket(config.getHost(), config.getPort());
			try
			{
				newTransport.open();
				isReady = true;
			} catch(TTransportException e)
			{
				LOG.debug("replicator {} still not ready for connections", config.getId());
				try
				{
					Thread.sleep(200);
				} catch(InterruptedException e1)
				{
					e1.printStackTrace();
				}
			} finally
			{
				newTransport.close();
			}

		} while(!isReady);

		ObjectPool<ReplicatorRPC.Client> pool = new ObjectPool<>();

		for(int i = 0; i < POOL_SIZE; i++)
		{
			ReplicatorRPC.Client rpcConnection = this.createReplicatorConnection(config);

			if(rpcConnection == null)
			{
				LOG.warn("error while creating connections for remote replicators: {}");
				continue;
			}

			pool.addObject(rpcConnection);
		}

		this.rpcsObjects.put(config.getId(), pool);
	}

	@Override
	public void sendOperationToRemote(ThriftOperation thriftOperation)
	{
		for(NodeConfig config : this.replicatorsConfigs.values())
		{
			ReplicatorRPC.Client rpcConnection = this.rpcsObjects.get(config.getId()).borrowObject();

			if(rpcConnection == null)
			{
				LOG.warn("no rpc connection available for replicator {}", config.getId());
				rpcConnection = this.createReplicatorConnection(config);
			}
			try
			{
				rpcConnection.commitOperationAsync(thriftOperation);
			} catch(TException e)
			{
				LOG.warn("failed to send shadow operation to replicator {}: {}", config.getId(), e.getMessage());
			}
		}
	}

	private ReplicatorRPC.Client createReplicatorConnection(NodeConfig config)
	{
		TTransport newTransport = new TSocket(config.getHost(), config.getPort());
		try
		{
			newTransport.open();
		} catch(TTransportException e)
		{
			LOG.error("error while creating connections for remote replicators: {}", e.getMessage());
			newTransport.close();
			return null;
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new ReplicatorRPC.Client(protocol);
	}
}
