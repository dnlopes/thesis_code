package nodes;


import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 23/05/15.
 * This interface exposes the delivery policy.
 */
public interface Deliver
{
	public void dispatchOperation(ShadowOperation op);
}
