package applications.tpcc;


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
		if(args.length != 8)
		{
			LOG.error("usage: java -jar <jarfile> <topologyFile> <environmentFile> <workloadFile> <emulatorId> " +
					"<numberClients> " +
					"<testDuration> <jdbc> [crdt || mysql] <numberWarehouses>");
			System.exit(-1);
		}

		String topologyFile = args[0];
		String envFile = args[1];
		String workloadFile = args[2];
		TpccConstants.WAREHOUSES_NUMBER = Integer.parseInt(args[7]);

		if(TpccConstants.WAREHOUSES_NUMBER > 1)
			TpccConstants.ALLOW_MULTI_WAREHOUSE_TX = true;
		else
			TpccConstants.ALLOW_MULTI_WAREHOUSE_TX = false;

		Topology.setupTopology(topologyFile);
		Environment.setupEnvironment(envFile);

		loadWorkloadFile(workloadFile);


		int proxyId = Integer.parseInt(args[3]);
		int numberClients = Integer.parseInt(args[4]);
		int testDuration = Integer.parseInt(args[5]);
		String jdbc = args[6];

		System.setProperty("jdbc", jdbc);
		System.setProperty("emulatorId", String.valueOf(proxyId));
		System.setProperty("proxyid", String.valueOf(proxyId));

		NodeConfig nodeConfig = Topology.getInstance().getProxyConfigWithIndex(proxyId);

		Workload workload = new TpccWorkload();

		TpccBenchmarkOptions options = new TpccBenchmarkOptions(numberClients, testDuration, jdbc, BENCHMARK_NAME,
				workload, nodeConfig.getDbProps());

		TPCCEmulator em = new TPCCEmulator(proxyId, options);
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
		System.out.println("CLIENT TERMINATED");
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
