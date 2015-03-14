package runtime;

import util.defaults.DBDefaults;


/**
 * Created by dnlopes on 13/03/15.
 */
public class Configuration
{

	private static final Configuration ourInstance = new Configuration();
	public static final String DB_NAME = "micro";
	public static final String SCHEMA_FILE = DBDefaults.MICRO_FILE;

	public static Configuration getInstance()
	{
		return ourInstance;
	}

	private Configuration()
	{
	}
}
