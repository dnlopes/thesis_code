package runtime;

import util.defaults.DBDefaults;


/**
 * Created by dnlopes on 13/03/15.
 */
public class Configuration
{

	public static final String DB_NAME = DBDefaults.TPCW_DB_NAME;
	public static final String SCHEMA_FILE = DBDefaults.TPCW_FILE;
	public static final String PROXY_HOSTNAME = "localhost";
	public static final int PROXY_PORT = 50000;
	public static final int PROXY_ID = 1;

	private Configuration()
	{
	}
}
