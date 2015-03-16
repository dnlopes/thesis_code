package tests;

import net.sf.appia.core.*;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer;

import net.sf.appia.test.appl.ApplLayer;
import network.INetwork;
import network.NetworkInterface;
import network.Node;
import network.Replicator;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkTest
{

	private static Layer[] qos = {new net.sf.appia.protocols.tcpcomplete.TcpCompleteLayer(),
			//new appia.protocols.sslcomplete.SslCompleteLayer(),
			new net.sf.appia.protocols.group.bottom.GroupBottomLayer(), new net.sf.appia.protocols.group.heal.GossipOutLayer(), new net.sf.appia.protocols.group.suspect.SuspectLayer(), new net.sf.appia.protocols.group.intra.IntraLayer(), new net.sf.appia.protocols.group.inter.InterLayer(), new net.sf.appia.protocols.group.heal.HealLayer(), new net.sf.appia.protocols.group.stable.StableLayer(), new net.sf.appia.protocols.group.leave.LeaveLayer(), new net.sf.appia.protocols.group.sync.VSyncLayer(), new ApplLayer(),};

	private static TcpCompleteLayer layer = new TcpCompleteLayer();

	public static void main(String args[])
			throws AppiaInvalidQoSException, AppiaCursorException, AppiaEventException, AppiaDuplicatedSessionsException
	{

		INetwork network = new NetworkInterface("localhost", 12345);

		Node node2 = new Replicator("localhost", 5, 12345);
		network.addNode(node2);

		network.sendBytes("asdasdsadsad".getBytes(), node2);

		System.out.println("AADASDSA");

	}

}
