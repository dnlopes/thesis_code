package network.node;

import java.net.InetSocketAddress;


/**
 * Created by dnlopes on 15/03/15.
 */
public abstract class AbstractNode
{

	private Role role;
	private int id;
	private InetSocketAddress socketAddress;

	public AbstractNode(String hostName, int port, int id, Role role)
	{
		this.id = id;
		this.role = role;
		this.socketAddress = new InetSocketAddress(hostName, port);
	}

	public Role getRole()
	{
		return this.role;
	}

	public int getId()
	{
		return this.id;
	}

	public InetSocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}

	public String getName()
	{
		return this.role + "-" + this.id;
	}
}

