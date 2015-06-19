package database.scratchpad;


import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 06/04/15.
 * Interface to execute operations in main storage
 */
public interface IDBCommitPad
{

	/**
	 * Attemps to commit a shadow transaction in main storage
	 * @param op
	 * @return true if commit succeeds
	 */
	public boolean commitShadowTransaction(ThriftShadowTransaction op);
}
