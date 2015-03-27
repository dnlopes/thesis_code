package network.proxy;


import network.AbstractNetwork;
import network.AbstractConfig;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.thrift.*;

import java.util.List;


/**
 * Created by dnlopes on 15/03/15.
 */
public class ProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ProxyNetwork.class);
	private int requestId;

	public ProxyNetwork(ProxyConfig node)
	{
		super(node);
		this.requestId = 0;
	}

	@Override
	public boolean commitOperation(ShadowOperation shadowOp, AbstractConfig node)
	{
		if(!this.replicatorsClients.containsKey(node.getName()))
			try
			{
				this.addNode(node);
			} catch(TTransportException e)
			{
				LOG.error("failed to bind with {}", node.getName());
				return false;
			}

		ReplicatorRPC.Client client = this.replicatorsClients.get(node.getName());
		ThriftOperation thriftOp = Utils.encodeThriftOperation(shadowOp);

		try
		{
			// this call blocks until the operation is committed in the main database
			return client.commitOperation(thriftOp);
		} catch(TException e)
		{
			LOG.error("thrift connection problem. Txn will abort");
			return false;
		}
	}

	@Override
	public ThriftCheckResponse checkInvariants(List<ThriftCheckEntry> checkList, AbstractConfig node) throws TException
	{
		if(!this.coordinatorsClients.containsKey(node.getName()))
			try
			{
				this.addNode(node);
			} catch(TTransportException e)
			{
				LOG.error("failed to bind with {}", node.getName());
				throw e;
			}

		CoordinatorRPC.Client client = this.coordinatorsClients.get(node.getName());
		ThriftCheckRequest newRequest = new ThriftCheckRequest(this.me.getName() + "(" + this.requestId++ + ")",
				checkList);

		return client.checkInvariants(newRequest);
	}

}
