package network.proxy;


import network.AbstractNodeConfig;
import network.Role;


/**
 * Created by dnlopes on 25/03/15.
 */
public class ProxyConfig extends AbstractNodeConfig
{

	private final AbstractNodeConfig replicatorConfig;
	private final AbstractNodeConfig coordinatorConfig;

	public ProxyConfig(int id, String host, int port, String dbHost, int dbPort, String dbUser, String dbPwd,
					   AbstractNodeConfig replicatorConfig, AbstractNodeConfig coordinatorConfig)
	{
		super(Role.PROXY, id, host, port, dbHost, dbPort, dbUser, dbPwd);

		this.replicatorConfig = replicatorConfig;
		this.coordinatorConfig = coordinatorConfig;
	}

	public AbstractNodeConfig getReplicatorConfig()
	{
		return this.replicatorConfig;
	}

	public AbstractNodeConfig getCoordinatorConfig()
	{
		return this.coordinatorConfig;
	}
}
