package applications.tpcc;


import applications.BenchmarkOptions;
import applications.Workload;
import util.DatabaseProperties;


/**
 * Created by dnlopes on 10/09/15.
 */
public class TpccBenchmarkOptions extends BenchmarkOptions
{

	private static final String ORIGINAL_DB_NAME = "tpcc";

	private final String databaseName;

	public TpccBenchmarkOptions(int clientsNumber, int duration, String jdbc, String name, Workload workload,
								DatabaseProperties dbProps)
	{
		super(clientsNumber, duration, jdbc, name, workload, dbProps);

		if(jdbc.compareTo(JDBCS.MYSQL_DRIVER) == 0)
			this.databaseName = ORIGINAL_DB_NAME;
		else
			this.databaseName = ORIGINAL_DB_NAME + "_crdt";

	}

	@Override
	public String getDatabaseName()
	{
		return this.databaseName;
	}
}
