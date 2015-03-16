package network;

import net.sf.appia.core.Channel;
import net.sf.appia.core.Event;
import net.sf.appia.core.Layer;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteSession;

import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public class TestSession extends TcpCompleteSession

{

	public TestSession(Layer layer)
	{
		super(layer);
	}

	public void sendStuff(String str, InetSocketAddress dest, Channel channel)
	{

		this.send(str.getBytes(), dest, channel);
	}


	public void handle(Event e)
	{
		System.out.println("handled");
	}


}
