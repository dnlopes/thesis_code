package applications.tpcc;


import applications.Emulator;
import applications.Workload;
import nodes.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Configuration;


/**
 * Created by dnlopes on 10/09/15.
 */
public class TpccBenchmark
{

	private static final Logger LOG = LoggerFactory.getLogger(TpccBenchmark.class);
	private static final String BENCHMARK_NAME = "TPCC";

	public static void main(String[] args)

	{
		if(args.length != 5)
		{
			LOG.error("usage: <configFile> <proxyId> <numberClients> <testDuration> <jdbc>");
			System.exit(1);
		}

		String configFile = args[0];
		Configuration.setupConfiguration(configFile);

		int proxyId = Integer.parseInt(args[1]);
		int numberClients = Integer.parseInt(args[2]);
		int testDuration = Integer.parseInt(args[3]);

		String jdbc = args[4];
		System.setProperty("proxyid", String.valueOf(proxyId));
		NodeConfig nodeConfig = Configuration.getInstance().getProxyConfigWithIndex(proxyId);

		Workload workload = new TpccWorkload();

		TpccBenchmarkOptions options = new TpccBenchmarkOptions(numberClients, testDuration, jdbc, BENCHMARK_NAME,
				workload, nodeConfig.getDbProps());

		Emulator em = new Emulator(proxyId, options);
		boolean success = em.runBenchmark();

		if(!success)
		{
			LOG.error("benchmark return error");
			em.shutdownEmulator();
			System.exit(-1);
		}

		em.printStatistics();
		em.shutdownEmulator();
		LOG.info("Benchmark ended!");
		System.exit(0);
	}
}
