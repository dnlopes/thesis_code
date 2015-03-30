package util.defaults;

/**
 * Created by dnlopes on 05/03/15.
 */
public class DBDefaults
{
	public static final String DEFAUTL_TPCW_PROPERTIES = "dbdefault.tpcw.properties";
	public static final String CRDT_TPCW_PROPERTIES = "crdtdb.tpcw.properties";

	public static final String MYSQL_HOST = "172.16.24.166";
	public static final String MYSQL_PORT = "3306";
	public static final String MYSQL_USER = "sa";
	public static final String MYSQL_PASSWORD = "101010";
	public static final String MYSQL_URL = MYSQL_HOST + ":" + MYSQL_PORT + "/";

	public static final String CRDT_URL_PREFIX = "jdbc:crdt://";
	public static final String DEFAULT_URL_PREFIX = "jdbc:mysql://";
	public static final String DEFAULT_URL = DEFAULT_URL_PREFIX + MYSQL_URL;
}