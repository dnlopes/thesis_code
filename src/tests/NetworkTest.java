package tests;

import applications.tpcw.TPCW_Database;
import net.sf.appia.core.*;

import network.INetwork;
import network.NetworkInterface;
import network.Node;
import network.Replicator;
import runtime.Configuration;


/**
 * Created by dnlopes on 15/03/15.
 */
public class NetworkTest
{

	public static void main(String args[])
			throws AppiaInvalidQoSException, AppiaCursorException, AppiaEventException, AppiaDuplicatedSessionsException
	{

		TPCW_Database.getBook(1);
		int davidPort = 45444;
		int ricardoPort = 32455;

		Node davidNode = new Replicator("localhost", davidPort, Configuration.PROXY_ID);
		Node ricardoNode = new Replicator("localhost", ricardoPort, 2);

		INetwork network;

		network = new NetworkInterface(davidNode);

		network.sendBytes("asdasdsadsad".getBytes(), davidNode);

		//network.sendBytes("cenas maradas".getBytes(), node2);

		network.sendBytes("cenas".getBytes(), ricardoNode);


		while(true)
		{

		}

	}

}
