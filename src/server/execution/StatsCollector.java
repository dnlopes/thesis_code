package server.execution;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 28/10/15.
 */
public class StatsCollector
{

	private AtomicInteger commitsCounter;
	private AtomicInteger retriesCounter;
	private AtomicInteger abortsCounter;

	public StatsCollector()
	{
		this.commitsCounter = new AtomicInteger();
		this.retriesCounter = new AtomicInteger();
		this.abortsCounter = new AtomicInteger();
	}

	public void incrementCommits()
	{
		this.commitsCounter.incrementAndGet();
	}

	public void incrementRetries()
	{
		this.retriesCounter.incrementAndGet();
	}

	public void incrementAborts()
	{
		this.abortsCounter.incrementAndGet();
	}

	public int getCommitsCounter()
	{
		return commitsCounter.get();
	}

	public int getRetriesCounter()
	{
		return retriesCounter.get();
	}

	public int getAbortsCounter()
	{
		return abortsCounter.get();
	}
}
