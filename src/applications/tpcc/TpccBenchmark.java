package applications.tpcc;


import applications.Emulator;
import applications.Workload;
import common.nodes.NodeConfig;
import common.util.Environment;
import common.util.Topology;
import common.util.exception.ConfigurationLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Created by dnlopes on 10/09/15.
 */
public class TpccBenchmark
{

	private static final Logger LOG = LoggerFactory.getLogger(TpccBenchmark.class);
	private static final String BENCHMARK_NAME = "TPCC Benchmark";

	public static void main(String[] args) throws ConfigurationLoadException
	{
		if(args.length != 7)
		{
			LOG.error("usage: java -jar <jarfile> <topologyFile> <environmentFile> <workloadFile> <proxyId> " +
					"<numberClients> " +
					"<testDuration> <jdbc> [crdt || mysql]");
			System.exit(-1);
		}

		String topologyFile = args[0];
		String environmentFile = args[2];
		String workloadFile = args[3];

		Topology.setupTopology(topologyFile);
		Environment.setupEnvironment(environmentFile);

		loadWorkloadFile(workloadFile);

		int proxyId = Integer.parseInt(args[4]);
		int numberClients = Integer.parseInt(args[5]);
		int testDuration = Integer.parseInt(args[6]);
		String jdbc = args[7];

		System.setProperty("proxyid", String.valueOf(proxyId));
		NodeConfig nodeConfig = Topology.getInstance().getProxyConfigWithIndex(proxyId);

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

	private static void loadWorkloadFile(String workloadFile)
	{
		Properties prop = new Properties();
		boolean missingValue = false;

		try
		{
			prop.load(new FileInputStream(workloadFile));

			if(!prop.containsKey("new_order"))
				missingValue = true;
			if(!prop.containsKey("payment"))
				missingValue = true;
			if(!prop.containsKey("delivery"))
				missingValue = true;
			if(!prop.containsKey("order_stat"))
				missingValue = true;
			if(!prop.containsKey("stock"))
				missingValue = true;

		} catch(IOException e)
		{
			LOG.error("failed to load workload file. Exiting...");
			System.exit(1);
		}

		if(missingValue)
		{
			LOG.error("failed to load workload file. Exiting...");
			System.exit(1);
		}

		TpccConstants.NEW_ORDER_TXN_RATE = Integer.parseInt(prop.getProperty("new_order"));
		TpccConstants.PAYMENT_TXN_RATE = Integer.parseInt(prop.getProperty("payment"));
		TpccConstants.DELIVERY_TXN_RATE = Integer.parseInt(prop.getProperty("delivery"));
		TpccConstants.ORDER_STAT_TXN_RATE = Integer.parseInt(prop.getProperty("order_stat"));
		TpccConstants.STOCK_LEVEL_TXN_RATE = Integer.parseInt(prop.getProperty("stock"));
	}
}
