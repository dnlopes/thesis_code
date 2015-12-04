package applications.tpcc;


import applications.TransactionStats;
import applications.util.TransactionRecord;

import java.util.*;


/**
 * Created by dnlopes on 26/05/15.
 */
public class TPCCStatistics
{

	private int threadId;
	private Map<String, List<TransactionRecord>> txnRecords;
	private List<TransactionStats> txnStats;

	public TPCCStatistics(int id)
	{
		this.threadId = id;
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

	public void printStatistics()
	{
		for(TransactionStats txnStat : this.txnStats)
			txnStat.printStats();
	}

	public List<TransactionStats> getTxnStats()
	{
		return this.txnStats;
	}

}
