package nodes;


import runtime.operation.ShadowTransaction;


/**
 * Created by dnlopes on 23/05/15.
 * This interface exposes the delivery policy.
 */
public interface Deliver
{
	public void dispatchOperation(ShadowTransaction op);
}
