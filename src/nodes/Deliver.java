package nodes;


import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 23/05/15.
 * This interface exposes the delivery policy.
 */
public interface Deliver
{
	public void dispatchOperation(ThriftShadowTransaction op);
}
