package util.defaults;

/**
 * Created by dnlopes on 05/03/15.
 */
public class DBDefaults
{
	public static final String CLOCK_VALUE_PLACEHOLDER = "'@clock@'";
	public static final String CONTENT_CLOCK_COLUMN = "_cclock";
	public static final String DELETED_CLOCK_COLUMN = "_dclock";
	public static final String DELETED_COLUMN = "_del";
	public static final String NOT_DELETED_VALUE = "0";
	public static final String COMPARE_CLOCK_FUNCTION = "compareClocks";
	public static final String CRDT_URL_PREFIX = "jdbc:crdt://";
	public static final String DEFAULT_URL_PREFIX = "jdbc:mysql://";
	public static final String DEFAULT_PASSWORD= "101010";
	public static final String DEFAULT_USER = "sa";
	public static final int DEFAULT_MYSQL_PORT = 3306;
}