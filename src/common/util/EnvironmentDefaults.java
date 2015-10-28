package common.util;


/**
 * Created by dnlopes on 28/10/15.
 */
public interface EnvironmentDefaults
{
	int EZK_CLIENTS_POOL_SIZE_DEFAULT = 20;
	int COMMIT_PAD_POOL_SIZE_DEFAULT = 50;
	boolean OPTIMIZE_BATCH_DEFAULT = true;
	int DISPATCHER_AGENT_DEFAULT = 2;
	int DELIVER_AGENT_DEFAULT = 1;


	String EZK_CLIENTS_POOL_SIZE_VAR = "ezk-client-pool-size";
	String COMMIT_PAD_POOL_SIZE_VAR = "commit-pool-size";
	String OPTIMIZE_BATCH_VAR = "optimize-batch";
	String EZK_EXTENSION_CODE_VAR = "ezk-extension-code-dir";
	String DATABASE_NAME_VAR = "dbname";
	String DISPATCHER_NAME_VAR = "dispatcher";
	String DELIVER_NAME_VAR = "deliver";
}
