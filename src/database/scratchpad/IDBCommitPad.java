package database.scratchpad;


import runtime.operation.ShadowOperation;


/**
 * Created by dnlopes on 06/04/15.
 * Interface to execute operations in main storage
 */
public interface IDBCommitPad
{

	/**
	 * Attemps to commit a shadow operation in main storage
	 * @param op
	 * @return true if commit succeeds
	 */
	public boolean commitShadowOperation(ShadowOperation op);
}
