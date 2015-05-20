package util.plot;


import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 20/05/15.
 */
public class CollectStatistics
{

	private static List<NodeStats> statsList = new ArrayList<>();

	public static void main(String args[]) throws IOException
	{
		if(args.length != 1)
		{
			System.out.println("usage: CollectStatistics <full dir directory>");
			System.exit(0);
		}

		String fullDir = args[0];
		File[] files = new File(fullDir).listFiles();
		parseFiles(files);
	}

	private static void parseFiles(File[] files) throws IOException
	{
		for(File file : files)
		{
			if(file.isDirectory())
				continue;
			if(!file.getName().contains("PROXY-"))
				continue;

			parseProxyFile(file);

		}
	}

	private static void parseProxyFile(File f) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(f));

		String line = br.readLine();
		line = br.readLine();

		String[] values = line.split("\t");
		long totalOps = Long.parseLong(values[1]);
		long commits = Long.parseLong(values[2]);
		long aborts = Long.parseLong(values[3]);
		long avgLatency = Long.parseLong(values[4]);

		NodeStats proxyStats = new NodeStats(totalOps, commits, aborts, avgLatency);
		statsList.add(proxyStats);
	}

	private static void mergeProxyStats()
	{

	}

	private static class NodeStats
	{

		public long getTotalOps()
		{
			return totalOps;
		}

		public long getNumberCommits()
		{
			return numberCommits;
		}

		public long getNumberAborts()
		{
			return numberAborts;
		}

		public long getAvgLatency()
		{
			return avgLatency;
		}

		private final long totalOps;
		private final long numberCommits;
		private final long numberAborts;
		private final long avgLatency;

		public NodeStats(long totalOps, long numberCommits, long numberAborts, long avgLatency)
		{
			this.totalOps = totalOps;
			this.numberCommits = numberCommits;
			this.numberAborts = numberAborts;
			this.avgLatency = avgLatency;
		}
	}
	
}
