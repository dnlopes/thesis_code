package network.server;


import network.node.Coordinator;
import network.service.CoordinatorService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CoordinatorRPC;


/**
 * Created by dnlopes on 20/03/15.
 */
public class CoordinatorServerThread implements Runnable
{

	static final Logger LOG = LoggerFactory.getLogger(CoordinatorServerThread.class);

	private Coordinator me;
	private CoordinatorService handler;
	private CoordinatorRPC.Processor processor;
	private TServer server;

	public CoordinatorServerThread(Coordinator node) throws TTransportException
	{
		this.me = node;
		this.handler = new CoordinatorService(this.me);
		this.processor = new CoordinatorRPC.Processor(handler);
		TServerTransport serverTransport = new TServerSocket(node.getSocketAddress().getPort());
		this.server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
	}

	@Override
	public void run()
	{
		LOG.info("starting coordinator server on port {}", this.me.getSocketAddress().getPort());
		this.server.serve();
	}
}
