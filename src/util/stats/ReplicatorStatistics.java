package util.stats;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by dnlopes on 06/04/15.
 */
public class ReplicatorStatistics implements Statistics
{

	AtomicInteger commitsCounter;
	AtomicInteger abortsCounter;
	AtomicLong latencySum;

	public ReplicatorStatistics()
	{
		this.commitsCounter = new AtomicInteger();
		this.abortsCounter = new AtomicInteger();
		this.latencySum = new AtomicLong();
	}

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();

		return "";
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

}
