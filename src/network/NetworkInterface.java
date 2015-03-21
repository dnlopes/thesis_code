package network;


import network.node.AbstractNode;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;

import java.util.*;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkInterface implements INetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(NetworkInterface.class);

	private AbstractNode me;
	private Map<String, ReplicatorRPC.Client> clients;

	public NetworkInterface(AbstractNode node) throws TTransportException
	{
		this.me = node;
		this.clients = new HashMap<>();
	}

	private void addNode(AbstractNode newNode)
	{
		if(newNode.getName().compareTo(this.me.getName()) == 0)
		{
			LOG.warn("should not be adding myself to the nodes list");
			return;
		}
		if(this.clients.containsKey(newNode.getName()))
		{
			LOG.warn("already have this node {}", newNode.getName());
			return;
		}

		TTransport newTransport = new TSocket(newNode.getSocketAddress().getHostName(),
				newNode.getSocketAddress().getPort());

		try
		{
			newTransport.open();
			TProtocol protocol = new TBinaryProtocol(newTransport);
			ReplicatorRPC.Client newClient = new ReplicatorRPC.Client(protocol);
			this.clients.put(newNode.getName(), newClient);
		} catch(TTransportException e)
		{
			e.printStackTrace();
		}

		LOG.trace("new node added {}", newNode.getName());
	}

	@Override
	public boolean commitOperation(ThriftOperation thriftOperation, AbstractNode node)
	{
		if(!this.clients.containsKey(node.getName()))
			this.addNode(node);

		ReplicatorRPC.Client client = this.clients.get(node.getName());
		//TODO: generate thrift operation
		try
		{
			return client.commitOperation(thriftOperation);
		} catch(TException e)
		{
			e.printStackTrace();
			return false;
		}

	}
}
