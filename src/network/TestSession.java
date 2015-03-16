package network;

import net.sf.appia.core.*;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.events.channel.ChannelInit;
import net.sf.appia.jgcs.protocols.top.SimpleTOPLayer;
import net.sf.appia.jgcs.protocols.top.SimpleTOPSession;
import net.sf.appia.protocols.common.RegisterSocketEvent;
import net.sf.appia.protocols.tcpcomplete.TcpCompleteSession;
import net.sf.appia.test.appl.ApplLayer;
import net.sf.appia.test.appl.ApplSession;
import net.sf.appia.test.xml.ecco.EccoLayer;
import net.sf.appia.test.xml.ecco.EccoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 15/03/15.
 */
public class TestSession extends EccoSession implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(TestSession.class);

	public TestSession(Node me, Node him)
	{
		super(new EccoLayer());
		this.init(me.getSocketAddress().getPort(), him.getSocketAddress());
	}

	@Override
	public void handle(Event e)
	{
		LOG.info("received event of type {}", e.getClass());
		super.handle(e);
	}


	@Override
	public void run()
	{
		Appia.run();
	}
}
