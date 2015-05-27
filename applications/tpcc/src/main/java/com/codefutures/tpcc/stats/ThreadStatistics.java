package com.codefutures.tpcc.stats;


/**
 * Created by dnlopes on 26/05/15.
 */
public class ThreadStatistics
{

	public int successCounter;
	public int abortsCounter;
	public long maxLatency, minLatency, avgLatency, latencySum;

	public ThreadStatistics()
	{
		this.successCounter = 0;
		this.abortsCounter = 0;
		this.maxLatency = 0;
		this.minLatency = 50000;
		this.avgLatency = 0;
		this.latencySum = 0;
	}

	public ThreadStatistics(ThreadStatistics stats)
	{
		this.successCounter = stats.successCounter;
		this.abortsCounter = stats.abortsCounter;
		this.latencySum = stats.latencySum;
		this.maxLatency = stats.maxLatency;
		this.minLatency = stats.minLatency;
	}

	public void addLatency(long latency)
	{
		if(latency > this.maxLatency)
			this.maxLatency = latency;

		if(latency < this.minLatency)
			this.minLatency = latency;

		this.latencySum += latency;
	}

	public void incrementSuccess()
	{
		this.successCounter++;
	}

	public void incrementAborts()
	{
		this.abortsCounter++;
	}

	public void calculateMissingStats()
	{
		this.avgLatency = this.latencySum / this.successCounter;
	}
}
