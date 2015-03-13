package util.defaults;

/**
 * Created by dnlopes on 05/03/15.
 */
public class DBDefaults
{

	public static final String TPCW_DB_NAME = "tpcw";
	public static final String MICRO_DB_NAME = "micro";

	public static final String TPCW_FILE = "/Users/dnlopes/devel/thesis/code/framework/application/tpcw/database.sql";
	public static final String MICRO_FILE = "/Users/dnlopes/devel/thesis/code/framework/application/micro/database.sql";

	public static final String MYSQL_HOST = "172.16.24.145";
	public static final String MYSQL_PORT = "3306";
	public static final String MYSQL_USER = "sa";
	public static final String MYSQL_PASSWORD = "101010";
	public static final String MYSQL_URL = MYSQL_HOST + ":" + MYSQL_PORT + "/";

	public static final String CRDT_URL_PREFIX = "jdbc:crdt://";
	public static final String DEFAULT_URL_PREFIX = "jdbc:mysql://";
	public static final String CRDT_URL = CRDT_URL_PREFIX + MYSQL_URL;
	public static final String DEFAULT_URL = DEFAULT_URL_PREFIX + MYSQL_URL;

	public static final int PAD_POOL_SIZE = 10;

}