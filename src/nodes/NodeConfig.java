package nodes;


import util.DatabaseProperties;


/**
 * Created by dnlopes on 22/03/15.
 */
public class NodeConfig
{

	private final int id;
	private final String host;
	private final int port;
	private final Role role;

	private final DatabaseProperties dbProps;

	public NodeConfig(Role role, int id, String host, int port, DatabaseProperties props)
	{
		this.role = role;
		this.id = id;
		this.host = host;
		this.port = port;
		this.dbProps = props;
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

	public String getName()
	{
		return this.role + "-" + this.id;
	}

	public Role getRole()
	{
		return this.role;
	}

	public DatabaseProperties getDbProps()
	{
		return this.dbProps;
	}
}
