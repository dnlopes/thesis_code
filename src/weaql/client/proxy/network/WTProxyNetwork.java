package weaql.client.proxy.network;


import weaql.client.execution.TransactionContext;
import weaql.common.nodes.AbstractNetwork;
import weaql.common.nodes.NodeConfig;
import weaql.common.thrift.RequestValue;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 07/12/15.
 */
public class WTProxyNetwork extends AbstractNetwork implements WTIProxyNetwork
{
	private static AtomicInteger id = new AtomicInteger(6000000);

	public WTProxyNetwork(NodeConfig node)
	{
		super(node);
	}


	public int requestNextId(RequestValue requestValue)
	{
		//TODO
		return id.incrementAndGet();
	}

	public String getTransactionClock()
	{
		//TODO
		return "1";
		//return null;
	}

	public void sendToRemoteReplicators(TransactionContext txnInfo)
	{
		//TODO
	}
}
