package network;


import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 23/05/15.
 */
public interface Deliver extends Runnable
{

	public void dispatchOperation(ShadowOperation op);
}
