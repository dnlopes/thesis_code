package nodes;


/**
 * Created by dnlopes on 22/03/15.
 */
public abstract class AbstractNodeConfig
{

	private final int id;
	private final String hostName;
	private final int port;
	private final String dbHost;
	private final int dbPort;
	private final String dbUser;
	private final String dbPwd;
	private final Role role;

	public AbstractNodeConfig(Role role, int id, String host, int port, String dbHost, int dbPort, String dbUser,
							  String dbPwd)
	{
		this.role = role;
		this.id = id;
		this.hostName = host;
		this.port = port;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
	}

	public int getId()
	{
		return id;
	}

	public String getHostName()
	{
		return hostName;
	}

	public int getPort()
	{
		return port;
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

	public String getName()
	{
		return this.role + "-" + this.id;
	}

	public Role getRole()
	{
		return this.role;
	}
}
