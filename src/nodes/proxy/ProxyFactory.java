package nodes.proxy;


import nodes.NodeConfig;
import runtime.IdentifierFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;


/**
 * Created by dnlopes on 02/09/15.
 */
public class ProxyFactory
{

	private static ProxyFactory ourInstance = new ProxyFactory();
	private static Proxy sharedProxy;
	private static int proxiesCounter;

	private static boolean useSharedProxy = Configuration.ProxyDefaults.USE_SHARED_PROXY;
	private static NodeConfig PROXY_CONFIG = Configuration.ProxyDefaults.PROXY_CONFIG;

	public static ProxyFactory getInstance()
	{
		return ourInstance;
	}

	private ProxyFactory()
	{
		proxiesCounter = 0;
		if(useSharedProxy)
			sharedProxy = new SharedProxy(PROXY_CONFIG);

		IdentifierFactory.setup(PROXY_CONFIG);
	}

	public static Proxy getProxyInstance()
	{
		if(useSharedProxy)
			return getSharedProxy();
		else
			return createSimpleProxy();
	}

	private static Proxy createSimpleProxy()
	{
		return new BasicProxy(PROXY_CONFIG, assignProxyId());
	}

	private static Proxy getSharedProxy()
	{
		if(sharedProxy == null)
			RuntimeUtils.throwRunTimeException("SharedProxy object is null", ExitCode.NULLPOINTER);

		return sharedProxy;
	}

	private static synchronized int assignProxyId()
	{
		return proxiesCounter++;
	}
}
