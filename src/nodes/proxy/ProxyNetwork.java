package nodes.proxy;


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

import runtime.RuntimeUtils;
import runtime.operation.ShadowOperation;
import util.ExitCode;
import util.ObjectPool;
import util.defaults.Configuration;
import util.thrift.*;


/**
 * Created by dnlopes on 15/03/15.
 */
public class ProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private final static int POOL_SIZE = 100;

	private final ObjectPool<ReplicatorRPC.Client> replicatorConnectionPool;
	private final ObjectPool<CoordinatorRPC.Client> coordinatorConnectionPool;

	private static final Logger LOG = LoggerFactory.getLogger(ProxyNetwork.class);

	public ProxyNetwork(ProxyConfig node)
	{
		super(node);
		this.coordinatorConnectionPool = new ObjectPool<>();
		this.replicatorConnectionPool = new ObjectPool<>();

		this.setup();
	}

	@Override
	public boolean commitOperation(ShadowOperation shadowOp, NodeConfig node)
	{
		ThriftOperation thriftOp = RuntimeUtils.encodeThriftOperation(shadowOp);

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
			return connection.commitOperation(thriftOp);
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

	@Override
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req, NodeConfig node)
	{
		req.setRequestId(0);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		CoordinatorRPC.Client connection = this.coordinatorConnectionPool.borrowObject();

		if(connection == null)
		{
			LOG.warn("coordinator connection pool empty. Creating new connection...");
			connection = this.createCoordinatorConnection(node);
		}

		try
		{
			return connection.checkInvariants(req);
		} catch(TException e)
		{
			return response;
		} finally
		{
			this.coordinatorConnectionPool.returnObject(connection);
		}
	}

	private CoordinatorRPC.Client createCoordinatorConnection(NodeConfig config)
	{
		TTransport newTransport = new TSocket(config.getHost(), config.getPort());
		try
		{
			newTransport.open();
		} catch(TTransportException e)
		{
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.NOINITIALIZATION);
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new CoordinatorRPC.Client(protocol);
	}

	private ReplicatorRPC.Client createReplicatorConnection(NodeConfig config)
	{
		TTransport newTransport = new TSocket(config.getHost(), config.getPort());
		try
		{
			newTransport.open();
		} catch(TTransportException e)
		{
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.NOINITIALIZATION);
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new ReplicatorRPC.Client(protocol);
	}

	private void setup()
	{
		if(Configuration.TRACE_ENABLED)
			LOG.trace("setting up client connections");
		ProxyConfig proxyConfig = (ProxyConfig) this.me;
		NodeConfig coordConfig = proxyConfig.getCoordinatorConfig();
		NodeConfig replicatorConfig = proxyConfig.getReplicatorConfig();

		for(int i = 0; i < POOL_SIZE; i++)
		{
			CoordinatorRPC.Client coordinatorConnection = this.createCoordinatorConnection(coordConfig);
			this.coordinatorConnectionPool.addObject(coordinatorConnection);
			ReplicatorRPC.Client replicatorConnection = this.createReplicatorConnection(replicatorConfig);
			this.replicatorConnectionPool.addObject(replicatorConnection);
		}
	}
}
