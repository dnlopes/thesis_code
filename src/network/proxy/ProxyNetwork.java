package network.proxy;


import network.AbstractNetwork;
import network.AbstractNodeConfig;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.thrift.*;


/**
 * Created by dnlopes on 15/03/15.
 */
public class ProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ProxyNetwork.class);

	public ProxyNetwork(AbstractNodeConfig node)
	{
		super(node);
	}

	@Override
	public boolean commitOperation(ShadowOperation shadowOp, AbstractNodeConfig node)
	{
		ThriftOperation thriftOp = Utils.encodeThriftOperation(shadowOp);
		TTransport newTransport = new TSocket(node.getHostName(), node.getPort());

		try
		{
			newTransport.open();
			TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
			ReplicatorRPC.Client client = new ReplicatorRPC.Client(protocol);
			// this call blocks until the operation is committed in the main database
			return client.commitOperation(thriftOp);
		} catch(TException e)
		{
			LOG.error("failed to contact replicator {}", node.getId(), e);
			return false;
		} finally
		{
			newTransport.close();
		}
	}

	@Override
	public CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req, AbstractNodeConfig node)
			throws TException
	{
		req.setRequestId(0);
		CoordinatorResponse response = new CoordinatorResponse();
		response.setSuccess(false);

		TTransport newTransport = null;
		try
		{
			newTransport = new TSocket(node.getHostName(), node.getPort());
			newTransport.open();
			TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
			CoordinatorRPC.Client client = new CoordinatorRPC.Client(protocol);

			// this call blocks until the operation is committed in the main database
			return client.checkInvariants(req);
		} catch(TException e)
		{
			LOG.error("failed to contact coordinator {}", node.getId(), e);
			return response;
		} finally
		{
			newTransport.close();
		}

	}

}
