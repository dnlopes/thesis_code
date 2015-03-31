package network.coordinator;


import network.AbstractNodeConfig;
import network.Role;
import network.replicator.ReplicatorConfig;


/**
 * Created by dnlopes on 22/03/15.
 */
public class CoordinatorConfig extends AbstractNodeConfig
{

	private final ReplicatorConfig replicatorConfig;

	public CoordinatorConfig(int id, String host, int port, String dbHost, int dbPort, String dbUser, String dbPwd,
							 ReplicatorConfig replicatorConfig)
	{
		super(Role.COORDINATOR, id, host, port, dbHost, dbPort, dbUser, dbPwd);
		this.replicatorConfig = replicatorConfig;
	}

	public ReplicatorConfig getReplicatorConfig()
	{
		return replicatorConfig;
	}
}
