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
import util.thrift.CRDTTransaction;
import util.thrift.ReplicatorRPC;


/**
 * Created by dnlopes on 02/09/15.
 */
public class ProxyNetwork extends AbstractNetwork implements IProxyNetwork
{

	private static final Logger LOG = LoggerFactory.getLogger(ProxyNetwork.class);

	private final NodeConfig replicatorConfig;
	private ReplicatorRPC.Client replicatorRpc;

	public ProxyNetwork(ProxyConfig proxyConfig)
	{
		super(proxyConfig);

		if(LOG.isTraceEnabled())
			LOG.trace("setting up rpc connection to replicator");

		this.replicatorConfig = proxyConfig.getReplicatorConfig();
		this.replicatorRpc = this.createReplicatorConnection();
	}

	@Override
	public boolean commitOperation(CRDTTransaction shadowTransaction, NodeConfig node)
	{
		try
		{
			return replicatorRpc.commitOperation(shadowTransaction);
		} catch(TException e)
		{
			LOG.warn("communication problem between proxy and replicator: {}", e.getMessage(), e);
			return false;
		}
	}

	private ReplicatorRPC.Client createReplicatorConnection()
	{
		TTransport newTransport = new TSocket(this.replicatorConfig.getHost(), this.replicatorConfig.getPort());
		try
		{
			newTransport.open();
		} catch(TTransportException e)
		{
			LOG.warn("failed to open connection to replicator node");
			return null;
		}

		TProtocol protocol = new TBinaryProtocol.Factory().getProtocol(newTransport);
		return new ReplicatorRPC.Client(protocol);
	}
}
