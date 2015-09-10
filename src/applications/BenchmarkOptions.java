package applications;


import util.DatabaseProperties;


/**
 * Created by dnlopes on 10/09/15.
 */
public abstract class BenchmarkOptions
{

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

	public interface JDBCS
	{

		public static String CRDT_DRIVER = "CRDT";
		public static String MYSQL_DRIVER = "MYSQL";
	}

	public interface Defaults
	{
		public static final int RAMPUP_TIME = 10;
	}
}
