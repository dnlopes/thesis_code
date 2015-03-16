package network;

import net.sf.appia.core.*;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer;
import net.sf.appia.test.appl.ApplLayer;

import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public class MyThread implements Runnable
{

	private static Layer[] qos = {new net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer(),
			//new appia.protocols.sslcomplete.SslCompleteLayer(),
			new net.sf.appia.protocols.group.bottom.GroupBottomLayer(), new net.sf.appia.protocols.group.heal.GossipOutLayer(), new net.sf.appia.protocols.group.suspect.SuspectLayer(), new net.sf.appia.protocols.group.intra.IntraLayer(), new net.sf.appia.protocols.group.inter.InterLayer(), new net.sf.appia.protocols.group.heal.HealLayer(), new net.sf.appia.protocols.group.stable.StableLayer(), new net.sf.appia.protocols.group.leave.LeaveLayer(), new net.sf.appia.protocols.group.sync.VSyncLayer(), new ApplLayer(),};

	private static TcpCompleteLayer layer = new TcpCompleteLayer();

	@Override
	public void run()
	{
		InetSocketAddress socketAddress = new InetSocketAddress("localhost", 15255);

		QoS myQoS = null;
		try
		{

			myQoS = new QoS("1", qos);

			TestSession session = new TestSession(layer);

			Channel myChannel = myQoS.createUnboundChannel("channel1");

			ChannelCursor cursor = myChannel.getCursor();

			cursor.bottom();
			cursor.setSession(session);
			myChannel.start();
			SendableEvent event = new SendableEvent(myChannel, Direction.DOWN, session);
			Message m = new Message();
			byte[] message = "OLA".getBytes();
			m.setByteArray(message, 0,message.length );
			event.setMessage(m);

			myChannel.getEventScheduler().insert(event);
			System.out.println("event inserted");
			Appia.run();


		} catch(AppiaDuplicatedSessionsException | AppiaCursorException | AppiaInvalidQoSException | AppiaEventException e)
		{
			e.printStackTrace();
		}

		//session.sendStuff("OLA", socketAddress, myChannel);

		System.out.println("yay");
	}
}
