package network.coordinator;


import network.AbstractConfig;
import network.Role;
import network.replicator.ReplicatorConfig;


/**
 * Created by dnlopes on 22/03/15.
 */
public class CoordinatorConfig extends AbstractConfig
{

	private final ReplicatorConfig replicatorConfig;

	public CoordinatorConfig(int id, String host, int port, ReplicatorConfig replicatorConfig)
	{
		super(Role.COORDINATOR, id, host, port);

		this.replicatorConfig = replicatorConfig;
	}

	public ReplicatorConfig getReplicatorConfig()
	{
		return replicatorConfig;
	}
}
