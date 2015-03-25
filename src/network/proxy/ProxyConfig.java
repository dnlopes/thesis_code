package network.proxy;


import network.AbstractConfig;
import network.Role;
import network.coordinator.CoordinatorConfig;
import network.replicator.ReplicatorConfig;


/**
 * Created by dnlopes on 25/03/15.
 */
public class ProxyConfig extends AbstractConfig
{

	private final String dbHost;
	private final int dbPort;
	private final String dbUser;
	private final String dbPwd;

	private final ReplicatorConfig replicatorConfig;
	private final CoordinatorConfig coordinatorConfig;

	public ProxyConfig(int id, String host, int port, String dbHost, int dbPort, String dbUser, String dbPwd,
					   ReplicatorConfig replicatorConfig, CoordinatorConfig coordinatorConfig)
	{
		super(Role.REPLICATOR, id, host, port);

		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbPwd = dbPwd;
		this.dbUser = dbUser;

		this.replicatorConfig = replicatorConfig;
		this.coordinatorConfig = coordinatorConfig;
	}

	public ReplicatorConfig getReplicatorConfig()
	{
		return this.replicatorConfig;
	}

	public CoordinatorConfig getCoordinatorConfig()
	{
		return this.coordinatorConfig;
	}

	public String getDbHost()
	{
		return this.dbHost;
	}

	public int getDbPort()
	{
		return this.dbPort;
	}

	public String getDbUser()
	{
		return this.dbUser;
	}

	public String getDbPwd()
	{
		return this.dbPwd;
	}
}
