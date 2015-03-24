package network.replicator;


import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
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
	private ReplicatorService handler;
	private ReplicatorRPC.Processor processor;
	private TServer server;

	public ReplicatorServerThread(Replicator node) throws TTransportException
	{
		this.me = node;
		this.handler = new ReplicatorService(this.me);
		this.processor = new ReplicatorRPC.Processor(handler);
		TServerTransport serverTransport = new TServerSocket(node.getSocketAddress().getPort());
		this.server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
	}

	@Override
	public void run()
	{
		LOG.info("starting replicator server on port {}", this.me.getSocketAddress().getPort());
		this.server.serve();
	}
}
