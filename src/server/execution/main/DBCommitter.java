package server.execution.main;


import common.thrift.CRDTCompiledTransaction;


/**
 * Created by dnlopes on 06/04/15.
 * Interface to execute shadow transactions on main storage
 */
public interface DBCommitter
{

	/**
	 * Attemps to commit a shadow transaction in main storage
	 * @param op
	 * @return true if commit succeeds
	 */
	boolean commitShadowTransaction(CRDTCompiledTransaction txn);


	interface Defaults
	{
		int LOG_FREQUENCY = 150;
	}
}
