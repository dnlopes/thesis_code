package client.proxy;


import common.Configuration;
import common.nodes.NodeConfig;


/**
 * Created by dnlopes on 02/09/15.
 */
public class ProxyFactory
{

	private static final ProxyFactory ourInstance = new ProxyFactory();
	private static NodeConfig PROXY_CONFIG;
	private static int proxiesCounter;

	public static ProxyFactory getInstance()
	{
		return ourInstance;
	}

	private ProxyFactory()
	{
		PROXY_CONFIG = Configuration.getInstance().getProxyConfigWithIndex(
				Integer.parseInt(System.getProperty("proxyid")));

		proxiesCounter = 0;
	}

	public static Proxy getProxyInstance()
	{
		return createSimpleProxy();
	}

	private static Proxy createSimpleProxy()
	{
		return new BasicProxy(PROXY_CONFIG, assignProxyId());
	}

	private static synchronized int assignProxyId()
	{
		return proxiesCounter++;
	}
}
