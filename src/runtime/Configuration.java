package runtime;

import util.defaults.DBDefaults;


/**
 * Created by dnlopes on 13/03/15.
 */
public class Configuration
{

	private static final Configuration ourInstance = new Configuration();
	public static String DB_NAME;
	public static String SCHEMA_FILE;

	public static Configuration getInstance()
	{
		return ourInstance;
	}

	private Configuration()
	{
		DB_NAME = "micro";
		SCHEMA_FILE = DBDefaults.MICRO_FILE;
	}
}
