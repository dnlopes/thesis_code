package nodes.proxy;


import nodes.NodeConfig;
import util.thrift.ThriftShadowTransaction;


/**
 * @author dnlopes
 *         This interface defines methods for all Proxy communications.
 *         All methods should be thread-safe, because this class is used by multiple threads to send events to
 *         remote nodes
 */
public interface IProxyNetwork
{

	public boolean commitOperation(ThriftShadowTransaction shadowTransaction, NodeConfig node);
}
