package network;

import network.node.AbstractNode;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 15/03/15.
 */
public interface INetwork
{
	public boolean commitOperation(ThriftOperation thriftOperation, AbstractNode node);
}
