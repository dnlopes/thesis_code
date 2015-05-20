package util.stats;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by dnlopes on 06/04/15.
 */
public class ProxyStatistics implements Statistics
{
	private String nodeName;
	private AtomicInteger commitsCounter;
	private AtomicInteger abortsCounter;
	private AtomicLong latencySum;

	public ProxyStatistics(String nodeName)
	{
		this.nodeName = nodeName;
		this.commitsCounter = new AtomicInteger();
		this.abortsCounter= new AtomicInteger();
		this.latencySum = new AtomicLong();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));

	}

	public void incrementCommitCounter()
	{
		this.commitsCounter.incrementAndGet();
	}

	public void incrementAbortsCounter()
	{
		this.abortsCounter.incrementAndGet();
	}

	public void addLatency(long latency)
	{
		this.latencySum.addAndGet(latency);
	}

	public String getResults()
	{
		long totalOps = commitsCounter.get() + abortsCounter.get();
		long avgLatency = 0;
		if(commitsCounter.get() > 0)
			avgLatency = latencySum.get() / commitsCounter.get();

		// TEMPLATE: NODE_NAME	TOTAL_OPS COMMITS_NUMBER ABORTS_NUMBER	AVG_LATENCY
		StringBuilder buffer = new StringBuilder();
		buffer.append("NODE_NAME\tTOTAL_OPS COMMITS_NUMBER ABORTS_NUMBER\tAVG_LATENCY\n");
		buffer.append(this.nodeName);
		buffer.append("\t");
		buffer.append(totalOps);
		buffer.append("\t");
		buffer.append(this.commitsCounter.get());
		buffer.append("\t");
		buffer.append(this.abortsCounter.get());
		buffer.append("\t");
		buffer.append(avgLatency);

		return buffer.toString();
	}

	private class ShutdownHook extends Thread
	{
		private ProxyStatistics stats;

		protected ShutdownHook(ProxyStatistics stats)
		{
			this.stats = stats;
		}

		@Override
		public void run()
		{
			String result = stats.getResults();
			try
			{
				PrintWriter out = new PrintWriter(this.stats.nodeName + ".out");
				out.write(result);
				out.close();
			} catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}

		}
	}
}
