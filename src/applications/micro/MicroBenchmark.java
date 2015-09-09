package applications.micro;


import applications.Emulator;
import applications.Workload;
import nodes.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import util.DatabaseProperties;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroBenchmark
{
	private static final Logger LOG = LoggerFactory.getLogger(MicroBenchmark.class);

	public static void main(String[] args)

	{
		if(args.length != 7)
		{
			LOG.error("usage: <configFile> <proxyId> <numberClients> <testDuration> <writePercentage> " +
					"<coordinatedPercentage> <customJDBC>");
			System.exit(1);
		}

		String configFile = args[0];
		int proxyId = Integer.parseInt(args[1]);
		int numberClients = Integer.parseInt(args[2]);
		int testDuration = Integer.parseInt(args[3]);
		int writeRate = Integer.parseInt(args[4]);
		int coordiantedRate = Integer.parseInt(args[5]);
		boolean customJDBC = Boolean.parseBoolean(args[6]);


		System.setProperty("configPath", configFile);
		System.setProperty("proxyid", String.valueOf(proxyId));
		System.setProperty("usersNum", String.valueOf(numberClients));
		System.setProperty("customJDBC", String.valueOf(customJDBC));

		NodeConfig nodeConfig = Configuration.getInstance().getProxyConfigWithIndex(proxyId);
		DatabaseProperties dbProperties = nodeConfig.getDbProps();

		Workload workload = new MicroWorkload(writeRate, coordiantedRate);
		Emulator em = new Emulator(proxyId, numberClients, testDuration, workload, dbProperties);

		boolean success = em.startBenchmark();

		if(!success)
		{
			LOG.error("benchmark return error");
			System.exit(-1);
		}

		em.printStatistics();
		LOG.info("Benchmark ended!");
		System.exit(0);
	}
}
