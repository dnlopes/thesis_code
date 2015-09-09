package applications;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.DatabaseProperties;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by dnlopes on 05/06/15.
 */
public class Emulator
{

	private static final Logger LOG = LoggerFactory.getLogger(Emulator.class);

	public static volatile boolean RUNNING;
	public static volatile boolean COUTING;
	private static int RAMPUP_TIME = 5;

	private String emulatorName;
	private int numberOfClients;
	private ExecutorService threadsService;
	private List<ClientEmulator> clients;
	private int benchmarkRuntime;
	private Workload workload;
	private int emulatorId;
	private DatabaseProperties dbProps;

	public Emulator(int id, int numberClients, int runtime, Workload workload, DatabaseProperties dbProps)
	{
		RUNNING = true;
		COUTING = false;
		this.emulatorId = id;
		this.numberOfClients = numberClients;
		this.threadsService = Executors.newFixedThreadPool(this.numberOfClients);
		this.clients = new ArrayList<>();
		this.benchmarkRuntime = runtime;
		this.workload = workload;
		this.dbProps = dbProps;
	}

	public Emulator(int id, String emulatorName, int numberClients, int runtime, Workload workload, DatabaseProperties
			dbProps)
	{
		RUNNING = true;
		COUTING = false;
		this.emulatorId = id;
		this.numberOfClients = numberClients;
		this.threadsService = Executors.newFixedThreadPool(this.numberOfClients);
		this.clients = new ArrayList<>();
		this.benchmarkRuntime = runtime;
		this.workload = workload;
		this.dbProps = dbProps;
		this.emulatorName = emulatorName;
	}

	public boolean startBenchmark()
	{
		System.out.println("****************************************");
		System.out.print("************ ");
		System.out.print(this.emulatorName);
		System.out.print(" ************");
		System.out.println();

		for(int i = 0; i < this.numberOfClients; i++)
		{
			Runnable client = new ClientEmulator(this.workload, this.dbProps);
			this.threadsService.execute(client);
			this.clients.add((ClientEmulator) client);
		}

		if(RAMPUP_TIME > 0)
		{
			System.out.println("Starting ramp up time...");
			try
			{
				Thread.sleep(RAMPUP_TIME * 1000);
			} catch(InterruptedException e)
			{
				LOG.error("ramp up time interrupted: {}", e.getMessage());
				return false;
			}
			System.out.println("Ramp up time ended!");
		}

		COUTING = true;
		final long startTime = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#,##0.0");
		long runTime;

		while((runTime = System.currentTimeMillis() - startTime) < this.benchmarkRuntime * 1000)
		{
			System.out.println("Current execution time lapse: " + df.format(runTime / 1000.0f) + " seconds");
			try
			{
				Thread.sleep(1000);
			} catch(InterruptedException e)
			{
				LOG.error("Benchmark was interrupted: {}", e.getMessage());
				return false;
			}
		}

		COUTING = false;
		RUNNING = false;
		final long actualTestTime = System.currentTimeMillis() - startTime;
		System.out.println("Experiment ended!");
		System.out.println("Benchmark elapsed time: " + df.format(actualTestTime / 1000.0f));
		return true;
	}

	public void printStatistics()
	{
		int abortCounter = 0;
		float avgLatency = 0;
		int opsCounter = 0;

		for(ClientEmulator client : this.clients)
		{
			avgLatency += client.getAverageLatency();
			opsCounter += client.getTotalOperations();
			abortCounter += client.getAbortCounter();
		}

		avgLatency = avgLatency / clients.size();

		StringBuilder buffer = new StringBuilder();
		boolean customJDBC = Boolean.parseBoolean(System.getProperty("customJDBC"));

		buffer.append("#writeRate,coordinationRate,avgLatency,commits,aborts,customJdbc\n");
		buffer.append(this.workload.getWriteRate());
		buffer.append(",");
		buffer.append(this.workload.getCoordinatedRate());
		buffer.append(",");
		buffer.append(avgLatency);
		buffer.append(",");
		buffer.append(opsCounter);
		buffer.append(",");
		buffer.append(abortCounter);
		buffer.append(",");
		buffer.append(String.valueOf(customJDBC));

		PrintWriter out;
		try
		{
			String fileName = this.getPrefix();
			out = new PrintWriter(fileName);
			out.write(buffer.toString());
			out.close();
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public String getPrefix()
	{
		return "Em" + this.emulatorId + "_" + this.numberOfClients + "users";
	}
}
