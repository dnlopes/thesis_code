package applications.micro;


import applications.micro.workload.Workload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.props.DatabaseProperties;

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

	public boolean startBenchmark()
	{
		System.out.println("****************************************");
		System.out.println("************ Microbenchmark ************");
		System.out.println("****************************************");

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
		float writeLatency = 0;
		float readLatency = 0;
		int writeCounter = 0;
		int readCounter = 0;
		int abortCounter = 0;

		for(ClientEmulator client : this.clients)
		{
			writeLatency += client.getTotalWriteLatency();
			readLatency += client.getTotalReadLatency();
			writeCounter += client.getSuccessCounterWrite();
			readCounter += client.getSuccessCounterRead();
			abortCounter += client.getAbortCounter();
		}

		float avgWriteLatency, avgReadLatency, avgLatencyTotal;

		if(writeCounter == 0)
			avgWriteLatency = 0;
		else
			avgWriteLatency = writeLatency / writeCounter;

		if(readCounter == 0)
			avgReadLatency = 0;
		else
			avgReadLatency = readLatency / readCounter;

		avgLatencyTotal = (writeLatency + readLatency) / (writeCounter + readCounter);

		StringBuilder buffer = new StringBuilder();
		boolean customJDBC = Boolean.parseBoolean(System.getProperty("customJDBC"));

		buffer.append("#writeRate,coordinationRate,avgWriteLatency,avgReadLatency,avgLatencyGlobal,aborts," +
				"customJdbc\n");
		buffer.append(this.workload.getWriteRate());
		buffer.append(",");
		buffer.append(this.workload.getCoordinatedRate());
		buffer.append(",");
		buffer.append(avgWriteLatency);
		buffer.append(",");
		buffer.append(avgReadLatency);
		buffer.append(",");
		buffer.append(avgLatencyTotal);
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
