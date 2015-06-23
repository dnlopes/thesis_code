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

import util.thrift.*;

import java.util.HashMap;
import java.util.Map;


public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final int POOL_SIZE = 100;
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	private Map<Integer, NodeConfig> replicatorsConfigs;
	private ObjectPool<CoordinatorRPC.Client> coordinatorConnectionPool;
	private final NodeConfig coordinatorConfig;

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();
		this.coordinatorConnectionPool = new ObjectPool<>();

		for(NodeConfig replicatorConfig : Configuration.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);

		this.coordinatorConfig = Configuration.getInstance().getCoordinatorConfigWithIndex(1);
		this.setup();
	}

	private void setup()
	{
		if(Configuration.TRACE_ENABLED)
			LOG.trace("creating connection pool to coordinator");

		for(int i = 0; i < POOL_SIZE; i++)
		{
			CoordinatorRPC.Client rpcConnection = this.createCoordinatorConnection();

			if(rpcConnection == null)
			{
				LOG.warn("failed to create connection for coordinator: {}");
				continue;
			}

			this.coordinatorConnectionPool.addObject(rpcConnection);
		}

		if(Configuration.DEBUG_ENABLED)
			LOG.debug("created {} connections to coordinator", this.coordinatorConnectionPool.getPoolSize());
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
		req.setRequestId(0);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		CoordinatorRPC.Client connection = this.coordinatorConnectionPool.borrowObject();

		if(connection == null)
		{
			LOG.warn("coordinator connection pool empty. Creating new connection...");
			connection = this.createCoordinatorConnection();
		}

		if(connection == null)
		{
			LOG.warn("failed to create coordinator connection");
			return response;
		}

		try
		{
			return connection.checkInvariants(req);
		} catch(TException e)
		{
			return response;
		} finally
		{
			if(connection != null)
				this.coordinatorConnectionPool.returnObject(connection);
		}
	}

	@Override
	public void releaseResources()
	{
		//TODO
	}

	private CoordinatorRPC.Client createCoordinatorConnection()
	{
		TTransport newTransport = new TSocket(this.coordinatorConfig.getHost(), this.coordinatorConfig.getPort());
		try
		{
			newTransport.open();
		} catch(TTransportException e)
		{
			LOG.warn("error while creating connections to coordinator: {}", e.getMessage());
			newTransport.close();
			return null;
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new CoordinatorRPC.Client(protocol);
	}

}
