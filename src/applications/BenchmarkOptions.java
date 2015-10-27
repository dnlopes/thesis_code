package applications;


import common.util.DatabaseProperties;


/**
 * Created by dnlopes on 15/09/15.
 */
public interface BenchmarkOptions
{

	public String getName();
	public String getJdbc();
	public int getDuration();
	public int getClientsNumber();
	public Workload getWorkload();
	public DatabaseProperties getDbProps();
	public abstract String getDatabaseName();
	public boolean isCRDTDriver();

	public interface JDBCS
	{

		public static String CRDT_DRIVER = "crdt";
		public static String MYSQL_DRIVER = "mysql";

		public static String[] JDBCS_ALLOWED = {CRDT_DRIVER, MYSQL_DRIVER};
	}

	public interface Defaults
	{

		public static final int RAMPUP_TIME = 10;
	}
}
