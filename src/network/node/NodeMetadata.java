package network.node;

/**
 * Created by dnlopes on 22/03/15.
 */
public class NodeMetadata
{

	private int id;
	private String host;
	private int port;
	private String dbHost;
	private int dbPort;
	private String dbUser;
	private String dbPwd;
	private Role role;
	private int refReplicator;

	// for building replicator
	public NodeMetadata(int id, String host, int port, String dbHost, int dbPort, String dbUser, String dbPwd,
						Role role)
	{
		//default for error
		this.refReplicator = -1;
		this.id = id;
		this.host = host;
		this.port = port;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		this.role = role;
	}

	// for building coordinator
	public NodeMetadata(int id, String host, int port, Role role)
	{
		//default for error
		this.refReplicator = -1;
		this.id = id;
		this.host = host;
		this.port = port;
		this.role = role;
	}

	// for building proxy
	public NodeMetadata(int refReplicator, int id, String host, int port, String dbHost, int dbPort, String dbUser,
						String dbPwd, Role role)
	{
		this.refReplicator = refReplicator;

		this.id = id;
		this.host = host;
		this.port = port;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
		this.role = role;
	}

	public int getId()
	{
		return id;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public String getDbHost()
	{
		return dbHost;
	}

	public int getDbPort()
	{
		return dbPort;
	}

	public String getDbUser()
	{
		return dbUser;
	}

	public String getDbPwd()
	{
		return dbPwd;
	}

	public String getName()
	{
		return this.role + "-" + this.id;
	}

	public int getRefReplicator()
	{
		return refReplicator;
	}

	public Role getRole()
	{
		return this.role;
	}
}
