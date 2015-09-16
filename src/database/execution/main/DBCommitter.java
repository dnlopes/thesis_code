package database.execution.main;


import util.thrift.ThriftShadowTransaction;


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
	public boolean commitShadowTransaction(ThriftShadowTransaction op);


	public interface Defaults
	{
		public static final int NUMBER_OF_RETRIES = 10;
		public static final int LOG_FREQUENCY = 150;
	}
}
