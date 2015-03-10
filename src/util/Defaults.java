package util;

/**
 * Created by dnlopes on 05/03/15.
 */
public class Defaults
{

	public static final String TPCW_DB_NAME = "tpcw";
	public static final String MYSQL_USER = "sa";
	public static final String MYSQL_PASSWORD = "101010";
	public static final String CRDT_URL_PREFIX = "jdbc:crdt://";
	public static final String DEFAULT_URL_PREFIX = "jdbc:mysql://";
	public static final String CRDT_URL = CRDT_URL_PREFIX + "localhost:53306/";
	public static final String DEFAULT_URL = DEFAULT_URL_PREFIX + "localhost:53306/";
	public static final int PAD_POOL_SIZE = 10;

}