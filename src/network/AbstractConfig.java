package network;


/**
 * Created by dnlopes on 22/03/15.
 */
public abstract class AbstractConfig
{

	private final int id;
	private final String hostName;
	private final int port;
	private final Role role;

	public AbstractConfig(Role role, int id, String host, int port)
	{
		this.role = role;
		this.id = id;
		this.hostName = host;
		this.port = port;
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


	public String getName()
	{
		return this.role + "-" + this.id;
	}

	public Role getRole()
	{
		return this.role;
	}
}
