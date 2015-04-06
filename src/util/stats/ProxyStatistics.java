package util.stats;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by dnlopes on 06/04/15.
 */
public class ProxyStatistics implements Statistics
{
	AtomicInteger commitsCounter;
	AtomicInteger abortsCounter;
	AtomicLong latencySum;

	public ProxyStatistics()
	{
		this.commitsCounter = new AtomicInteger();
		this.abortsCounter= new AtomicInteger();
		this.latencySum = new AtomicLong();
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

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();

		return "";
	}
}
