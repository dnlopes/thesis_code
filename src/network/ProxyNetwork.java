package network;


import network.node.AbstractNode;
import org.apache.thrift.TException;
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
	public boolean commitOperation(ShadowOperation shadowOp, AbstractNode node)
	{
		if(!this.clients.containsKey(node.getName()))
			this.addNode(node);

		ReplicatorRPC.Client client = this.clients.get(node.getName());
		ThriftOperation thriftOp = Utils.encodeThriftOperation(shadowOp);

		try
		{
			// this call blocks until the operation is committed in the main database
			return client.commitOperation(thriftOp);
		} catch(TException e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
