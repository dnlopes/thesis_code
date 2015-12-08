package client.proxy.network;


import common.thrift.CRDTPreCompiledTransaction;
import common.thrift.Status;


/**
 * @author dnlopes
 *         This interface defines methods for all Proxy communications.
 *         All methods should be thread-safe, because this class is used by multiple threads to send events to
 *         remote nodes
 */
public interface IProxyNetwork
{

	Status commitOperation(CRDTPreCompiledTransaction shadowTransaction);
}
