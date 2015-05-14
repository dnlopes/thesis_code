package util.defaults;

/**
 * Created by dnlopes on 05/03/15.
 */
public class DBDefaults
{

	public static final String CONTENT_CLOCK_COLUMN = "_cclock";
	public static final String DELETED_CLOCK_COLUMN = "_dclock";
	public static final String DELETED_COLUMN = "_del";
	public static final String CLOCK_VALUE_PLACEHOLDER = "@_cclock@";
	public static final String DELETED_CLOCK_PLACEHOLDER = "@_dclock@";
	public static final String CLOCK_GENERATION_PLACEHOLDER = "_cgen_placeholder";


	public static final String DATABASE_PROPERTIES_FILE = "database.properties";

	public static final String CRDT_URL_PREFIX = "jdbc:crdt://";
	public static final String DEFAULT_URL_PREFIX = "jdbc:mysql://";
}