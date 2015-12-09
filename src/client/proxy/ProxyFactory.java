package client.proxy;


import common.nodes.NodeConfig;
import common.util.Topology;

import java.sql.SQLException;


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
		PROXY_CONFIG = Topology.getInstance().getProxyConfigWithIndex(Integer.parseInt(System.getProperty("proxyid")));

		proxiesCounter = 0;
	}

	public static Proxy getProxyInstance() throws SQLException
	{
		//return createWriteThroughProxy();
		//return createBasicProxy();
		return createWSSandboxProxy();
	}

	private static Proxy createWriteThroughProxy()
	{
		return new WriteThroughProxy(PROXY_CONFIG, assignProxyId());
	}

	private static Proxy createBasicProxy() throws SQLException
	{
		return new SandboxProxy(PROXY_CONFIG, assignProxyId());
	}

	private static Proxy createWSSandboxProxy() throws SQLException
	{
		return new SandboxWriteSetProxy(PROXY_CONFIG, assignProxyId());
	}

	private static synchronized int assignProxyId()
	{
		return proxiesCounter++;
	}
}
