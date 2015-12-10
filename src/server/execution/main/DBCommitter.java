package server.execution.main;


import common.thrift.CRDTCompiledTransaction;
import common.thrift.Status;
import server.util.TransactionCommitFailureException;


/**
 * Created by dnlopes on 06/04/15.
 * Interface to execute shadow transactions on main storage
 */
public interface DBCommitter
{

	int MAX_RETRIES = 50;

	/**
	 * Attemps to commit a transaction in main storage
	 * @param op
	 * @return true if commit succeeds
	 */
	Status commitTrx(CRDTCompiledTransaction txn) throws TransactionCommitFailureException;
}
