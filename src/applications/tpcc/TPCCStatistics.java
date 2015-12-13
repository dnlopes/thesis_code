package applications.tpcc;


import applications.TransactionStats;
import applications.util.TransactionRecord;

import java.util.*;


/**
 * Created by dnlopes on 26/05/15.
 */
public class TPCCStatistics
{

	private Map<String, List<TransactionRecord>> txnRecords;
	private List<TransactionStats> txnStats;

	public TPCCStatistics(int id)
	{
		this.txnRecords = new HashMap<>();
		this.txnStats = new ArrayList<>();

		for(String txnName : TpccConstants.TXNS_NAMES)
			this.txnRecords.put(txnName, new LinkedList<TransactionRecord>());
	}

	public void addTxnRecord(String txnName, TransactionRecord record)
	{
		this.txnRecords.get(txnName).add(record);
	}

	public void mergeStatistics(TPCCStatistics otherStats)
	{
		for(Map.Entry<String, List<TransactionRecord>> entry : otherStats.getTxnRecords().entrySet())
			this.txnRecords.get(entry.getKey()).addAll(entry.getValue());
	}

	public Map<String, List<TransactionRecord>> getTxnRecords()
	{
		return this.txnRecords;
	}

	public void generateStatistics()
	{
		for(Map.Entry<String, List<TransactionRecord>> entry : this.txnRecords.entrySet())
		{
			TransactionStats txnStats = new TransactionStats(entry.getKey(), entry.getValue());
			this.txnStats.add(txnStats);
		}
	}

	public String getStatsString()
	{
		StringBuilder buffer = new StringBuilder();

		for(TransactionStats txnStat : this.txnStats)
			buffer.append(txnStat.getStatsString());

		return buffer.toString();
	}

	public String getStatsString(int iteration)
	{
		StringBuilder buffer = new StringBuilder();

		for(TransactionStats txnStat : this.txnStats)
			buffer.append(txnStat.getStatsString(iteration));

		return buffer.toString();
	}

	public String getDistributionStrings()
	{
		StringBuilder buffer = new StringBuilder();

		for(TransactionStats txnStat : this.txnStats)
			buffer.append(txnStat.getDistributionStatsString());

		return buffer.toString();
	}
}
