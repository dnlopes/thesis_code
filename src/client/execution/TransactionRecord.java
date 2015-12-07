package client.execution;


import java.util.*;


/**
 * Created by dnlopes on 07/12/15.
 */
public class TransactionRecord
{

	private long startTime;
	private long endTime;
	private long selectsTime;
	private long updatesTime;
	private long insertsTime;
	private long deletesTime;
	private long parsingTime;
	private long execTime;
	private long commitTime;
	private long loadFromMainTime;
	private List<String> crdtOps;


	public TransactionRecord()
	{
		this.selectsTime = 0;
		this.updatesTime = 0;
		this.insertsTime = 0;
		this.deletesTime = 0;
		this.execTime = 0;
		this.commitTime = 0;
		this.parsingTime = 0;
		this.loadFromMainTime = 0;
		this.crdtOps = new LinkedList<>();
	}

	public void setStartTime(long time)
	{
		this.startTime = time;
	}

	public void setEndTime(long time)
	{
		this.endTime = time;
	}

	public void setExecTime(long time)
	{
		this.execTime = time;
	}

	public void setCommitTime(long time)
	{
		this.commitTime = time;
	}

	public void addSelectTime(long time)
	{
		this.selectsTime += time;
	}

	public void addDeleteTime(long time)
	{
		this.deletesTime += time;
	}

	public void addUpdateTime(long time)
	{
		this.updatesTime += time;
	}

	public void addInsertTime(long time)
	{
		this.insertsTime += time;
	}

	public void addToParsingTime(long time)
	{
		this.parsingTime += time;
	}

	public void addLoadfromMainTime(long time)
	{
		this.loadFromMainTime += time;
	}

	public double getSelectsTime()
	{
		return selectsTime * 0.000001;
	}

	public double getDeletesTime()
	{
		return deletesTime * 0.000001;
	}

	public double getUpdatesTime()
	{
		return updatesTime * 0.000001;
	}

	public double getInsertsTime()
	{
		return insertsTime * 0.000001;
	}

	public double getExecTime()
	{
		return execTime * 0.000001;
	}

	public double getCommitTime()
	{
		return commitTime * 0.000001;
	}

	public double getParsingTime()
	{
		return parsingTime * 0.000001;
	}

	public double getLoadFromMainTime()
	{
		return loadFromMainTime * 0.000001;
	}

	public void addCrdtOp(String crdtOp)
	{
		crdtOps.add(crdtOp);
	}

	public void addCrdtOp(String[] crdtOpList)
	{
		crdtOps.addAll(Arrays.asList(crdtOpList));
	}

	public long getStartTime()
	{
		return startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void printRecord()
	{
		System.out.println("*** INFO ***");
		System.out.println("selects time (ms): " + getSelectsTime());
		System.out.println("inserts time (ms): " + getInsertsTime());
		System.out.println("updates time (ms): " + getUpdatesTime());
		System.out.println("deletes time (ms): " + getDeletesTime());
		System.out.println("load from main time (ms): " + getLoadFromMainTime());
		System.out.println("parsing time (ms): " + getParsingTime());
		System.out.println("exec time (ms): " + getExecTime());
		System.out.println("commit time (ms): " + getCommitTime());
		System.out.println();
	}

	public void clear()
	{
		this.startTime = 0;
		this.endTime = 0;
		this.selectsTime = 0;
		this.updatesTime = 0;
		this.execTime = 0;
		this.commitTime = 0;
		this.insertsTime = 0;
		this.deletesTime = 0;
		this.parsingTime = 0;
		this.loadFromMainTime = 0;
		this.crdtOps.clear();
	}
}
