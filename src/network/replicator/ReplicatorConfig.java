package network.replicator;


import network.AbstractNodeConfig;
import network.Role;


/**
 * Created by dnlopes on 25/03/15.
 */
public class ReplicatorConfig extends AbstractNodeConfig
{

	public ReplicatorConfig(int id, String host, int port, String dbHost, int dbPort, String dbUser, String dbPwd)
	{
		super(Role.REPLICATOR, id, host, port, dbHost, dbPort, dbUser, dbPwd);
	}

}
