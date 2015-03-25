package network.replicator;


import network.AbstractConfig;
import network.Role;


/**
 * Created by dnlopes on 25/03/15.
 */
public class ReplicatorConfig extends AbstractConfig
{

	private final String dbHost;
	private final int dbPort;
	private final String dbUser;
	private final String dbPwd;

	public ReplicatorConfig(int id, String host, int port, String dbHost, int dbPort, String dbUser,
							String dbPwd)
	{
		super(Role.REPLICATOR, id, host, port);

		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbPwd = dbPwd;
		this.dbUser = dbUser;
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
