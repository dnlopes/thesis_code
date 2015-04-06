package database.scratchpad;


import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 06/04/15.
 */
public interface IDBCommitPad
{

	public boolean commitShadowOperation(ShadowOperation op);
	public long getCommitLatency();
}
