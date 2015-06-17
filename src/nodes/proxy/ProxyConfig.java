package nodes.proxy;


import nodes.NodeConfig;
import nodes.Role;
import util.DatabaseProperties;


/**
 * Created by dnlopes on 25/03/15.
 */
public class ProxyConfig extends NodeConfig
{

	private final NodeConfig replicatorConfig;
	private final NodeConfig coordinatorConfig;

	public ProxyConfig(int id, String host, int port, DatabaseProperties dbProps,
					   NodeConfig replicatorConfig, NodeConfig coordinatorConfig)
	{
		super(Role.PROXY, id, host, port, dbProps);

		this.replicatorConfig = replicatorConfig;
		this.coordinatorConfig = coordinatorConfig;
	}

	public NodeConfig getReplicatorConfig()
	{
		return this.replicatorConfig;
	}

	public NodeConfig getCoordinatorConfig()
	{
		return this.coordinatorConfig;
	}
}
