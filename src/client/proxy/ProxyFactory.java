package client.proxy;


import common.Configuration;


/**
 * Created by dnlopes on 02/09/15.
 */
public class ProxyFactory
{

	private static final ProxyFactory ourInstance = new ProxyFactory();
	private static ProxyConfig PROXY_CONFIG;
	private static int proxiesCounter;

	public static ProxyFactory getInstance()
	{
		return ourInstance;
	}

	private ProxyFactory()
	{
		PROXY_CONFIG = (ProxyConfig) Configuration.getInstance().getProxyConfigWithIndex(
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
