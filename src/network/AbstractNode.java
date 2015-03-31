package network;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public abstract class AbstractNode
{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractNode.class);

	private InetSocketAddress socketAddress;
	protected AbstractNodeConfig config;

	public AbstractNode(AbstractNodeConfig config)
	{
		LOG.trace("setup abstract node");
		this.socketAddress = new InetSocketAddress(config.getHostName(), config.getPort());
		this.config = config;
	}

	public InetSocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}

	public AbstractNodeConfig getConfig()
	{
		return this.config;
	}
}


