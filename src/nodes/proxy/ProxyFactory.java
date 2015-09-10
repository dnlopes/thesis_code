package nodes.proxy;


import runtime.IdentifierFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.Configuration;


/**
 * Created by dnlopes on 02/09/15.
 */
public class ProxyFactory
{

	private static final ProxyFactory ourInstance = new ProxyFactory();

	private static boolean USE_SHARED_PROXY;
	private static ProxyConfig PROXY_CONFIG;

	private static Proxy sharedProxy;
	private static int proxiesCounter;

	public static ProxyFactory getInstance()
	{
		return ourInstance;
	}

	private ProxyFactory()
	{
		USE_SHARED_PROXY = Configuration.getInstance().useSharedProxy();
		PROXY_CONFIG = (ProxyConfig) Configuration.getInstance().getProxyConfigWithIndex(
			Integer.parseInt(System.getProperty("proxyid")));

		proxiesCounter = 0;
		if(USE_SHARED_PROXY)
			sharedProxy = new SharedProxy(PROXY_CONFIG);

		IdentifierFactory.setup(PROXY_CONFIG);
	}

	public static Proxy getProxyInstance()
	{
		if(USE_SHARED_PROXY)
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
