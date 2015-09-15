package applications;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 05/06/15.
 */
public class Emulator
{

	private static final Logger LOG = LoggerFactory.getLogger(Emulator.class);

	public static volatile boolean RUNNING;
	public static volatile boolean COUTING;

	private ExecutorService threadsService;
	private List<ClientEmulator> clients;
	private int emulatorId;
	private BaseBenchmarkOptions options;

	public Emulator(int id, BaseBenchmarkOptions options)
	{
		RUNNING = true;
		COUTING = false;
		this.emulatorId = id;
		this.options = options;
		this.clients = new ArrayList<>();
		this.threadsService = Executors.newFixedThreadPool(this.options.getClientsNumber());
	}

	public boolean runBenchmark()
	{
		System.out.println("****************************************");
		System.out.print("************  ");
		System.out.print(this.options.getName());
		System.out.print("  ************");
		System.out.println();
		System.out.println("Number of clients: " + this.options.getClientsNumber());
		System.out.println("JDBC driver: " + this.options.getJdbc());
		System.out.println("Benchmark duration: " + this.options.getDuration());
		System.out.println("****************************************");

		for(int i = 0; i < this.options.getClientsNumber(); i++)
		{
			ClientEmulator client = new ClientEmulator(this.options);
			this.clients.add(client);
			this.threadsService.execute(client);
		}

		if(BenchmarkOptions.Defaults.RAMPUP_TIME > 0)
		{
			System.out.println("Starting ramp up time...");
			try
			{
				Thread.sleep(BenchmarkOptions.Defaults.RAMPUP_TIME * 1000);
			} catch(InterruptedException e)
			{
				LOG.error("ramp up time interrupted: {}", e.getMessage());
				this.shutdownEmulator();
				return false;
			}
			System.out.println("Ramp up time ended!");
		}

		COUTING = true;
		final long startTime = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#,##0.0");
		long runTime;

		while((runTime = System.currentTimeMillis() - startTime) < this.options.getDuration() * 1000)
		{
			System.out.println("Current execution time lapse: " + df.format(runTime / 1000.0f) + " seconds");
			try
			{
				Thread.sleep(1000);
			} catch(InterruptedException e)
			{
				LOG.error("Benchmark was interrupted: {}", e.getMessage());
				this.shutdownEmulator();
				return false;
			}
		}

		RUNNING = false;
		COUTING = false;

		final long actualTestTime = System.currentTimeMillis() - startTime;

		System.out.println("Experiment ended!");
		System.out.println("Benchmark elapsed time: " + df.format(actualTestTime / 1000.0f));

		return true;
	}

	public void printStatistics()
	{
		int abortCounter = 0;
		float avgLatency = 0, avgReadLatency = 0, avgWriteLatency = 0;
		int opsCounter = 0;

		for(ClientEmulator client : this.clients)
		{
			//TODO merge stats
			avgLatency += client.getAverageLatency();
			avgReadLatency += client.getAverageReadLatency();
			avgWriteLatency+= client.getAverageWriteLatency();
			opsCounter += client.getTotalOperations();
			abortCounter += client.getAbortCounter();
		}

		avgLatency = avgLatency / clients.size();
		avgReadLatency = avgReadLatency / clients.size();
		avgWriteLatency= avgWriteLatency / clients.size();
		StringBuilder buffer = new StringBuilder();

		//TODO
		buffer.append("#writeRate,coordinationRate,avgLatency,avgReadLatency,avgWriteLatency,commits,aborts,jdbc," +
				"users");

		this.options.getWorkload().addExtraColumns(buffer);

		buffer.append("\n");
		buffer.append(options.getWorkload().getWriteRate());
		buffer.append(",");
		buffer.append(this.options.getWorkload().getCoordinatedOperationsRate());
		buffer.append(",");
		buffer.append(avgLatency);
		buffer.append(",");
		buffer.append(avgReadLatency);
		buffer.append(",");
		buffer.append(avgWriteLatency);
		buffer.append(",");
		buffer.append(opsCounter);
		buffer.append(",");
		buffer.append(abortCounter);
		buffer.append(",");
		buffer.append(options.getJdbc());
		buffer.append(",");
		buffer.append(options.getClientsNumber());

		this.options.getWorkload().addExtraColumnValues(buffer);

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
		return "Em" + this.emulatorId + "_" + this.options.getClientsNumber() + "users";
	}

	public void shutdownEmulator()
	{
		COUTING = false;
		RUNNING = false;
		this.threadsService.shutdown();

		try
		{
			this.threadsService.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch(InterruptedException e)
		{
			LOG.error(e.getMessage(), e);
		}
	}

}
