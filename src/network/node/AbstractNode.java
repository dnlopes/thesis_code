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

	public AbstractNode(NodeMedatada nodeInfo)
	{
		this.id = nodeInfo.getId();
		this.role = nodeInfo.getRole();
		this.socketAddress = new InetSocketAddress(nodeInfo.getHost(), nodeInfo.getPort());
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


