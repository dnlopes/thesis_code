package network;


import network.node.AbstractNode;
import network.node.NodeMedatada;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.ReplicatorRPC;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public class AbstractNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractNetwork.class);

	protected AbstractNode me;
	protected Map<String, ReplicatorRPC.Client> clients;

	public AbstractNetwork(AbstractNode node)
	{
		this.me = node;
		this.clients = new HashMap<>();
	}

	protected void addNode(NodeMedatada newNode)
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

		TTransport newTransport = new TSocket(newNode.getHost(),
				newNode.getPort());

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
}
