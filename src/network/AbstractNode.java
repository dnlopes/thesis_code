package network;


import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public abstract class AbstractNode
{

	private InetSocketAddress socketAddress;
	protected AbstractConfig config;

	public AbstractNode(AbstractConfig config)
	{
		this.socketAddress = new InetSocketAddress(config.getHostName(), config.getPort());
		this.config = config;
	}

	public InetSocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}

	public AbstractConfig getConfig()
	{
		return this.config;
	}
}


