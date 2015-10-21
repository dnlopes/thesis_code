package nodes.replicator;


import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.ReplicatorRPC;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorServerThread implements Runnable
{

	static final Logger LOG = LoggerFactory.getLogger(ReplicatorServerThread.class);

	private Replicator me;
	private TServer server;

	public ReplicatorServerThread(Replicator node) throws TTransportException
	{
		this.me = node;
		ReplicatorService handler = new ReplicatorService(this.me);
		ReplicatorRPC.Processor processor = new ReplicatorRPC.Processor(handler);
		TServerTransport serverTransport = new TServerSocket(node.getSocketAddress().getPort());
		this.server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
	}

	@Override
	public void run()
	{
		if(LOG.isInfoEnabled())
			LOG.info("starting replicator server on port {}", this.me.getSocketAddress().getPort());
		this.server.serve();
	}
}
