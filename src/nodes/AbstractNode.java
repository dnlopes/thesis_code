package nodes;


import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public abstract class AbstractNode
{

	private InetSocketAddress socketAddress;
	protected AbstractNodeConfig config;

	public AbstractNode(AbstractNodeConfig config)
	{
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


