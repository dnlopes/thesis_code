package network;


import network.node.AbstractNode;
import network.node.NodeMetadata;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 15/03/15.
 */
public class ProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ProxyNetwork.class);

	public ProxyNetwork(AbstractNode node)
	{
		super(node);
	}

	@Override
	public boolean commitOperation(ShadowOperation shadowOp, NodeMetadata node)
	{
		if(!this.clients.containsKey(node.getName()))
			try
			{
				this.addNode(node);
			} catch(TTransportException e)
			{
				LOG.error("failed to bind with {}", node.getName());
				return false;
			}

		ReplicatorRPC.Client client = this.clients.get(node.getName());
		ThriftOperation thriftOp = Utils.encodeThriftOperation(shadowOp);

		try
		{
			// this call blocks until the operation is committed in the main database
			return client.commitOperation(thriftOp);
		} catch(TException e)
		{
			LOG.error("failed to commit shadow op {}", shadowOp.getTxnId());
			return false;
		}
	}
}
