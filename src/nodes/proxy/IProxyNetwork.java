package nodes.proxy;


import nodes.NodeConfig;
import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 21/03/15.
 */
public interface IProxyNetwork
{
	public boolean commitOperation(ThriftShadowTransaction shadowTransaction, NodeConfig node);
}
