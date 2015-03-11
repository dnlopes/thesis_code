package runtime;

import org.perf4j.StopWatch;
import util.LogicalClock;
import util.TimeStamp;


/**
 * accumulates information on a transaction as it is processed
 */
public class TransactionInfo
{

	private long txnId;
	private TimeStamp timestamp;
	private LogicalClock lc;
	private StopWatch timer;
	private long latency;
	private Operation shadowOp;

	public TransactionInfo(long txnId)
	{
		this.txnId = txnId;
		this.shadowOp = null;
		this.timer = new StopWatch(String.valueOf(this.txnId));
	}

	public long getTxnId()
	{
		return this.txnId;
	}

	public void setTxnId(long id)
	{
		this.txnId = id;
	}

	public TimeStamp getTimestamp()
	{
		return this.timestamp;
	}

	public void setTimestamp(TimeStamp timestamp)
	{
		this.timestamp = timestamp;
	}

	public LogicalClock getLogicalClock()
	{
		return this.lc;
	}

	public void setLogicalClock(LogicalClock lc)
	{
		this.lc = lc;
	}

	public Operation getShadowOp()
	{
		return this.shadowOp;
	}

	public void setShadowOp(Operation op)
	{
		this.shadowOp = op;
	}

	public long getLatency()
	{
		return this.latency;
	}

	public void start()
	{
		this.timer.start();
	}

	public void stop()
	{
		this.timer.stop();
		this.latency = this.timer.getElapsedTime();
	}

	public void clear()
	{
		this.txnId = 0;
		this.timestamp = null;
		this.lc = null;
		this.shadowOp = null;
		this.timer = null;
	}
}