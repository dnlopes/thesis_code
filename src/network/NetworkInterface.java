package network;

import net.sf.appia.core.*;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;

import net.sf.appia.test.appl.ApplLayer;

import net.sf.appia.test.xml.ecco.EccoLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Runtime;
import runtime.operation.Operation;
import util.ExitCode;

import java.util.*;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkInterface implements INetwork
{

	private static Layer[] QOS_LAYERS = {new EccoLayer(), new net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer(),
			//new appia.protocols.sslcomplete.SslCompleteLayer(),
			new net.sf.appia.protocols.group.bottom.GroupBottomLayer(), new net.sf.appia.protocols.group.heal.GossipOutLayer(), new net.sf.appia.protocols.group.suspect.SuspectLayer(), new net.sf.appia.protocols.group.intra.IntraLayer(), new net.sf.appia.protocols.group.inter.InterLayer(), new net.sf.appia.protocols.group.heal.HealLayer(), new net.sf.appia.protocols.group.stable.StableLayer(), new net.sf.appia.protocols.group.leave.LeaveLayer(), new net.sf.appia.protocols.group.sync.VSyncLayer(), new ApplLayer(),};

	private static final Logger LOG = LoggerFactory.getLogger(NetworkInterface.class);

	private static QoS qos;

	private Map<String, TestSession> sessions;
	private Map<String, Channel> channels;
	private Node me;

	public NetworkInterface(Node node)
	{
		try
		{
			qos = new QoS("1", QOS_LAYERS);
		} catch(AppiaInvalidQoSException e)
		{
			LOG.error("failed to init QoS");
			Runtime.throwRunTimeException("could not create network interface", ExitCode.QOS_ERROR);
		}
		this.me = node;
		this.channels = new HashMap<>();
		this.sessions = new HashMap<>();

		this.addNode(node);

		Thread t = new Thread(sessions.get(this.me.getName()));
		t.start();

	}

	public void sendOperation(Operation op)
	{
		//TODO
	}

	@Override
	public void sendToNode(Operation op, Node node)
	{
		//TODO
	}

	@Override
	public void addNode(Node newNode)
	{
		//TODO
		LOG.trace("new channel added to node {}", newNode.getName());
	}

	@Override
	public void sendBytes(byte[] message, Node to)
	{
		if(!channels.containsKey(to.getName()))
			this.addNode(to);

		SendableEvent event = new SendableEvent();

		Message m = new Message();
		try
		{
			Channel channel = channels.get(to.getName());
			TestSession session = sessions.get(to.getName());

			m.setByteArray(message, 0, message.length);

			event.setMessage(m);
			event.setDir(Direction.DOWN);
			event.setChannel(channel);
			event.setSourceSession(session);
			event.init();
			channel.getEventScheduler().insert(event);
			//event.asyncGo(channel, Direction.DOWN);
			LOG.info("event of type {} sent", event.getClass());

		} catch(AppiaEventException e)
		{
			LOG.error("failed to create new event to node {}", to.getName());
		}

	}

}
