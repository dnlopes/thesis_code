package applications;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseProperties;


/**
 * Created by dnlopes on 10/09/15.
 */
public abstract class BenchmarkOptions
{

	private static final Logger LOG = LoggerFactory.getLogger(BenchmarkOptions.class);

	private final int clientsNumber;
	private final int duration;
	private final String jdbc;
	private final String name;
	private final Workload workload;
	private final DatabaseProperties dbProps;

	public BenchmarkOptions(int clientsNumber, int duration, String jdbc, String name, Workload workload,
							DatabaseProperties dbProps)
	{
		this.clientsNumber = clientsNumber;
		this.duration = duration;
		this.jdbc = jdbc;
		this.name = name;
		this.workload = workload;
		this.dbProps = dbProps;

		if(!this.isValidJdbc(this.jdbc))
		{
			LOG.error("invalid jdbc");
			System.exit(-1);
		}
	}

	public String getName()
	{
		return this.name;
	}

	public String getJdbc()
	{
		return this.jdbc;
	}

	public int getDuration()
	{
		return this.duration;
	}

	public int getClientsNumber()
	{
		return this.clientsNumber;
	}

	public Workload getWorkload()
	{
		return this.workload;
	}

	public DatabaseProperties getDbProps()
	{
		return this.dbProps;
	}

	public boolean isCRDTDriver()
	{
		return this.jdbc.compareTo(JDBCS.CRDT_DRIVER) == 0;
	}

	public abstract String getDatabaseName();

	private boolean isValidJdbc(String jdbc)
	{
		for(String driver : JDBCS.JDBCS_ALLOWED)
		{
			if(jdbc.compareTo(driver) == 0)
				return true;
		}

		return false;
	}

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
