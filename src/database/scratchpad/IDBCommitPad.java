package database.scratchpad;


import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 06/04/15.
 * Interface to execute operations in main storage
 */
public interface IDBCommitPad
{

	public boolean commitShadowOperation(ShadowOperation op);
}
