package applications.micro;


import applications.micro.workload.MicroWorkload;
import applications.micro.workload.Workload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroBenchmark
{
	private static final Logger LOG = LoggerFactory.getLogger(MicroBenchmark.class);
	private static final int NUMBER_OF_TABLES = 4;

	public static void main(String[] args)

	{
		if(args.length != 3)
		{
			LOG.error("usage: <numberClients> <testDuration> <writePercentage>");
			System.exit(1);
		}
		int numberClients = Integer.parseInt(args[0]);
		int testDuration = Integer.parseInt(args[1]);
		int writePercentage = Integer.parseInt(args[2]);

		//Workload workload = new MicroWorkload(writePercentage, NUMBER_OF_TABLES);

		//Emulator em = new Emulator(numberClients, testDuration, workload);

		//em.startBenchmark();
		//em.collectStatistics();
	}
}
