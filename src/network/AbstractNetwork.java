package network;


import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.thrift.CoordinatorRPC;
import util.thrift.ReplicatorRPC;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public abstract class AbstractNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractNetwork.class);

	protected AbstractNodeConfig me;
	protected Map<String, ReplicatorRPC.Client> replicatorsClients;
	protected Map<String, CoordinatorRPC.Client> coordinatorsClients;


	public AbstractNetwork(AbstractNodeConfig node)
	{
		this.me = node;
		this.replicatorsClients = new HashMap<>();
		this.coordinatorsClients = new HashMap<>();

	}

	protected void addNode(AbstractNodeConfig newNode) throws TTransportException
	{
		if(newNode.getName().compareTo(this.me.getName()) == 0)
		{
			LOG.warn("should not be adding myself to the nodes list");
			return;
		}

		if(newNode.getRole() == Role.COORDINATOR)
			this.addCoordinatorNode(newNode);
		else if(newNode.getRole() == Role.REPLICATOR)
			this.addReplicatorNode(newNode);
		else
			RuntimeHelper.throwRunTimeException("unexpected node type", ExitCode.UNEXPECTED_OP);

		LOG.trace("new node added {}", newNode.getName());
	}

	private void addReplicatorNode(AbstractNodeConfig newNode) throws TTransportException
	{
		if(this.replicatorsClients.containsKey(newNode.getName()))
		{
			LOG.warn("already have this node {}", newNode.getName());
			return;
		}

		TTransport newTransport = new TSocket(newNode.getHostName(), newNode.getPort());

		newTransport.open();
		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		ReplicatorRPC.Client newClient = new ReplicatorRPC.Client(protocol);
		this.replicatorsClients.put(newNode.getName(), newClient);
	}

	private void addCoordinatorNode(AbstractNodeConfig newNode) throws TTransportException
	{
		if(this.coordinatorsClients.containsKey(newNode.getName()))
		{
			LOG.warn("already have this node {}", newNode.getName());
			return;
		}

		TTransport newTransport = new TSocket(newNode.getHostName(), newNode.getPort());

		newTransport.open();
		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		CoordinatorRPC.Client newClient = new CoordinatorRPC.Client(protocol);
		this.coordinatorsClients.put(newNode.getName(), newClient);
	}
}
