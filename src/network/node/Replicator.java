package network.node;


import network.server.ReplicatorServerThread;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{

	static final Logger LOG = LoggerFactory.getLogger(Replicator.class);

	private ReplicatorServerThread serverThread;

	public Replicator(String hostName, int id, int port) throws TTransportException
	{
		super(hostName, id, port, Role.REPLICATOR);

		this.serverThread = new ReplicatorServerThread(this);

		new Thread(this.serverThread).start();
	}

}
