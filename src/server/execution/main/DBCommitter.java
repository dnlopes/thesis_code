package server.execution.main;


import common.thrift.CRDTPreCompiledTransaction;
import common.thrift.Status;
import server.util.TransactionCommitFailureException;


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
	Status commitCrdtOperation(CRDTPreCompiledTransaction txn) throws TransactionCommitFailureException;


	interface Defaults
	{
		int LOG_FREQUENCY = 150;
	}
}
