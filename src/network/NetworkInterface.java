package network;

import net.sf.appia.core.*;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.protocols.tcpcomplete.*;
import net.sf.appia.test.appl.ApplLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.Operation;
import util.ExitCode;
import runtime.Runtime;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkInterface extends TcpCompleteSession implements INetwork
{

	private static final TcpCompleteLayer layer = new TcpCompleteLayer();
	private static final Logger LOG = LoggerFactory.getLogger(NetworkInterface.class);

	private static Layer[] qos = {new net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer(),
			//new appia.protocols.sslcomplete.SslCompleteLayer(),
			new net.sf.appia.protocols.group.bottom.GroupBottomLayer(), new net.sf.appia.protocols.group.heal.GossipOutLayer(), new net.sf.appia.protocols.group.suspect.SuspectLayer(), new net.sf.appia.protocols.group.intra.IntraLayer(), new net.sf.appia.protocols.group.inter.InterLayer(), new net.sf.appia.protocols.group.heal.HealLayer(), new net.sf.appia.protocols.group.stable.StableLayer(), new net.sf.appia.protocols.group.leave.LeaveLayer(), new net.sf.appia.protocols.group.sync.VSyncLayer(), new ApplLayer(),};

	private static QoS myQoS;

	private InetSocketAddress mySocketAddr;
	private Map<Node, InetSocketAddress> otherReplicas;

	public NetworkInterface(String host, int port)
	{
		super(layer);
		try
		{
			myQoS = new QoS("QOS_1", qos);
		} catch(AppiaInvalidQoSException e)
		{
			LOG.error("failed to create QoS for network interface");
			Runtime.throwRunTimeException("failed to create QoS for network interface", ExitCode.QOS_ERROR);
		}

		this.mySocketAddr = new InetSocketAddress(host, port);
		this.otherReplicas = new Hashtable<>();
		this.init();
		LOG.info("listening on port {}", this.ourPort);
	}

	public void sendOperation(Operation op)
	{

	}

	@Override
	public void sendToNode(Operation op, Node node)
	{

	}

	@Override
	public void addNode(Node newNode)
	{
		Channel newChannel = myQoS.createUnboundChannel(newNode.getName());
		ChannelCursor cursor = newChannel.getCursor();
		cursor.bottom();

		try
		{
			cursor.setSession(this);
			newChannel.start();
			super.createSocket(this.ourReaders, newNode.getSocketAddress(), newChannel);

		} catch(AppiaCursorException | IOException | AppiaDuplicatedSessionsException e)
		{
			LOG.error("failed to create channel for node {}", newNode.getName());
			e.printStackTrace();
			Runtime.throwRunTimeException("failed to create channel", ExitCode.CHANNEL_ERROR);
		}

		LOG.trace("new channel added to node {}", newNode.getName());
	}

	@Override
	public void sendBytes(byte[] message, Node to)
	{
		SendableEvent event;
		Message m = new Message();
		try
		{
			Channel channel = channels.get(to.getName());
			event = new SendableEvent(channel, Direction.DOWN, this);
			event.setSourceSession(this);
			m.setByteArray(message, 0, message.length);
			event.setMessage(m);
			event.init();
			channel.getEventScheduler().insert(event);

		} catch(AppiaEventException e)
		{
			LOG.error("failed to create new event to node {}", to.getName());
			return;
		}

	}

	private void init()
	{
		//TODO
		// 1- init otherReplicas map
		// 2- create channels for each entry
		//this.createSocket(this.ch)
	}

	@Override
	protected void handleRegisterSocket(RegisterSocketEvent e)
	{

		if(LOG.isDebugEnabled())
			LOG.debug("TCP Session received RegisterSocketEvent to register a socket in port " + e.port);
		ServerSocket ss = null;

		if(ourPort < 0)
		{
			if(e.port == RegisterSocketEvent.FIRST_AVAILABLE)
			{
				try
				{
					ss = new ServerSocket(0, 50, e.localHost);
				} catch(IOException ex)
				{
					LOG.debug("Exception when trying to create a server socket in First Available mode: " + ex);
				}
			} else if(e.port == RegisterSocketEvent.RANDOMLY_AVAILABLE)
			{
				final Random rand = new Random();
				int p;
				boolean done = false;

				while(!done)
				{
					p = rand.nextInt(Short.MAX_VALUE);

					try
					{
						ss = new ServerSocket(p, 50, e.localHost);
						done = true;
					} catch(IllegalArgumentException | IOException ex)
					{
						LOG.debug("Exception when trying to create a server socket in Randomly Available mode: " + ex);
					}
				}
			} else
			{
				try
				{
					ss = new ServerSocket(e.port, 50, e.localHost);
				} catch(IOException ex)
				{
					LOG.debug(
							"Exception when trying to create a server socket using the port: " + e.port + "\nException: " + ex);
				}
			}
		}
		if(ss != null)
		{
			ourPort = ss.getLocalPort();
			if(LOG.isDebugEnabled())
				LOG.debug("TCP Session registered a socket in port " + ourPort);

			//create accept thread int the request port.
			acceptThread = new AcceptReader(ss, this, e.getChannel(), socketLock);
			final Thread t = e.getChannel().getThreadFactory().newThread(acceptThread);
			t.setName("TCP Accept thread from port " + ourPort);
			t.start();

			e.localHost = ss.getInetAddress();
			e.port = ourPort;
			e.error = false;
		} else
		{
			e.error = true;
			if(acceptThread != null && acceptThread.getPort() == e.port)
			{
				e.setErrorCode(RegisterSocketEvent.RESOURCE_ALREADY_BOUND_ERROR);
				e.setErrorDescription("Socket already bound in port " + e.port);
			} else
			{
				e.setErrorCode(RegisterSocketEvent.RESOURCE_BUSY_ERROR);
				e.setErrorDescription("Could not create socket. Resource is busy.");
			}
		}

		//		send RegisterSocketEvent
		e.setDir(Direction.invert(e.getDir()));
		e.setSourceSession(this);

		try
		{
			e.init();
			e.go();
		} catch(AppiaEventException ex)
		{
			ex.printStackTrace();
		}
	}

}
