package server.replicator;


import common.nodes.AbstractNetwork;
import common.nodes.NodeConfig;
import common.util.Environment;
import common.util.Topology;
import common.util.exception.SocketConnectionException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.util.ObjectPool;
import common.thrift.*;
import server.agents.coordination.zookeeper.EZKCoordinationClient;
import server.agents.coordination.zookeeper.EZKCoordinationExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class ReplicatorNetwork extends AbstractNetwork implements IReplicatorNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorNetwork.class);

	private Map<Integer, NodeConfig> replicatorsConfigs;
	private ObjectPool<EZKCoordinationClient> ezkClientsPool;
	private int clientsCount;
	private AtomicLong totalLatency = new AtomicLong();
	private AtomicInteger counter = new AtomicInteger();

	public ReplicatorNetwork(NodeConfig node)
	{
		super(node);
		this.replicatorsConfigs = new HashMap<>();
		this.clientsCount = this.me.getId();
		this.ezkClientsPool = new ObjectPool<>();

		for(NodeConfig replicatorConfig : Topology.getInstance().getAllReplicatorsConfig().values())
			if(replicatorConfig.getId() != this.me.getId())
				this.replicatorsConfigs.put(replicatorConfig.getId(), replicatorConfig);

		initZookeeperConnections();
	}

	@Override
	public void sendOperationToRemote(CRDTCompiledTransaction transaction)
	{
		for(NodeConfig config : replicatorsConfigs.values())
		{
			long start = System.nanoTime();
			TTransport newTransport = new TSocket(config.getHost(), config.getPort());

			try
			{
				newTransport.open();
				TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
				ReplicatorRPC.Client client = new ReplicatorRPC.Client(protocol);
				long estimated = System.nanoTime() - start;
				totalLatency.addAndGet(estimated);
				client.sendToRemote(transaction);
			} catch(TException e)
			{
				LOG.warn("failed to trx to replicator {}: ", config.getId(), e);
			} finally
			{
				newTransport.close();
			}
		}

		int tmp = counter.incrementAndGet();
		long latencyShot = totalLatency.get();
		double latency = latencyShot * 0.000001;
		if(tmp % 100 == 0)
			LOG.info("time spent so far creating connections: {} ms", latency);
	}

	@Override
	public void sendBatchToRemote(List<CRDTCompiledTransaction> transactions)
	{
		for(NodeConfig config : this.replicatorsConfigs.values())
		{
			TTransport newTransport = new TSocket(config.getHost(), config.getPort());

			try
			{
				newTransport.open();
				TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
				ReplicatorRPC.Client client = new ReplicatorRPC.Client(protocol);
				client.sendBatchToRemote(transactions);
			} catch(TException e)
			{
				LOG.warn("failed to send batch to replicator {}", config.getId(), e);
			} finally
			{
				newTransport.close();
			}
		}
	}

	@Override
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req) throws SocketConnectionException
	{
		CoordinatorResponse dummyResponse = new CoordinatorResponse();
		dummyResponse.setSuccess(false);

		EZKCoordinationClient client = this.ezkClientsPool.borrowObject();

		if(client == null)
		{
			LOG.warn("zookeeper connections pool empty. creating new connection");

			client = createZookeeperConnection();

			if(client == null)
			{
				LOG.error("failed to create new zookeeper connection");
				throw new SocketConnectionException("failed to establish connection with zookeeper cluster");
			}
		}

		CoordinatorResponse response = client.sendRequest(req);
		this.ezkClientsPool.returnObject(client);

		return response;
	}

	private void initZookeeperConnections()
	{
		if(Environment.IS_ZOOKEEPER_REQUIRED)
		{
			LOG.info("opening {} zookeeper connections", Environment.EZK_CLIENTS_POOL_SIZE);

			for(int i = 0; i < Environment.EZK_CLIENTS_POOL_SIZE; i++)
			{
				EZKCoordinationClient newClient = createZookeeperConnection();

				if(newClient != null)
					this.ezkClientsPool.addObject(newClient);
			}
			LOG.info("{} zookeeper connections ready", ezkClientsPool.getPoolSize());
		}
	}

	private EZKCoordinationClient createZookeeperConnection()
	{
		ZooKeeper zooKeeper;
		try
		{
			zooKeeper = new ZooKeeper(Topology.ZOOKEEPER_CONNECTION_STRING,
					EZKCoordinationExtension.ZookeeperDefaults.ZOOKEEPER_SESSION_TIMEOUT, null);
		} catch(IOException e)
		{
			LOG.warn(e.getMessage());
			return null;
		}

		this.clientsCount += Topology.getInstance().getReplicatorsCount();
		EZKCoordinationClient client = new EZKCoordinationClient(zooKeeper, this.clientsCount);

		try
		{
			client.init(Environment.EZK_EXTENSION_CODE);
		} catch(KeeperException | InterruptedException e)
		{
			LOG.warn("failed to install zookeeper extension {}: ", e.getMessage(), e);
			return null;
		}

		return client;
	}
}
