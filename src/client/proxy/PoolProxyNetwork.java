package client.proxy;


import common.nodes.AbstractNetwork;
import common.nodes.NodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.util.ObjectPool;
import common.thrift.*;


/**
 * Created by dnlopes on 15/03/15.
 */
public class PoolProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private final static int POOL_SIZE = 100;

	private final ObjectPool<ReplicatorRPC.Client> replicatorConnectionPool;

	private static final Logger LOG = LoggerFactory.getLogger(PoolProxyNetwork.class);

	public PoolProxyNetwork(ProxyConfig node)
	{
		super(node);
		this.replicatorConnectionPool = new ObjectPool<>();

		this.setup();
	}

	@Override
	public boolean commitOperation(CRDTTransaction shadowTransaction, NodeConfig node)
	{

		ReplicatorRPC.Client connection = this.replicatorConnectionPool.borrowObject();

		if(connection == null)
		{
			LOG.warn("replicator connection pool empty. Creating new connection...");
			connection = this.createReplicatorConnection(node);
		}

		if(connection == null)
		{
			LOG.warn("failed to create connection to replicator");
			return false;
		}

		try
		{
			return connection.commitOperation(shadowTransaction);
		} catch(TException e)
		{
			LOG.warn("communication problem between proxy and replicator: {}", e.getMessage(), e);
			return false;
		} finally
		{
			if(connection != null)
				this.replicatorConnectionPool.returnObject(connection);
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
			LOG.warn("failed to open connection to replicator node");
			return null;
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new ReplicatorRPC.Client(protocol);
	}

	private void setup()
	{
		if(LOG.isTraceEnabled())
			LOG.trace("setting up client connections");

		ProxyConfig proxyConfig = (ProxyConfig) this.me;
		NodeConfig replicatorConfig = proxyConfig.getReplicatorConfig();

		for(int i = 0; i < POOL_SIZE; i++)
		{
			ReplicatorRPC.Client replicatorConnection = this.createReplicatorConnection(replicatorConfig);

			if(replicatorConnection != null)
				this.replicatorConnectionPool.addObject(replicatorConnection);
		}
	}
}
