package server.hook;


import server.execution.StatsCollector;


/**
 * Created by dnlopes on 02/12/15.
 */
public class PrinterHook extends Thread
{
	private final StatsCollector stats;

	public PrinterHook(StatsCollector stats)
	{
		this.stats = stats;
	}

	@Override
	public void run() {
		System.out.println("avg commit time: " + stats.getAverageLatency());
		System.out.println("commited txns: " + stats.getCommitsCounter());
	}
}
