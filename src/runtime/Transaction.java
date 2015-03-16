package runtime;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import util.LogicalClock;
import util.TimeStamp;


/**
 * accumulates information on a transaction as it is processed
 */
public class Transaction
{

	static final Logger LOG = LoggerFactory.getLogger(Transaction.class);

	private long txnId;
	private long latency;
	private boolean hasBegun;
	private boolean hasEnded;
	private TimeStamp timestamp;
	private LogicalClock lc;
	private StopWatch timer;
	private ShadowOperation shadowOp;

	public Transaction()
	{
		this.txnId = 0;
		this.latency = 0;
		this.hasBegun = false;
		this.hasEnded = false;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.timer = new StopWatch();
	}

	public long getTxnId()
	{
		return this.txnId;
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

	public ShadowOperation getShadowOp()
	{
		return this.shadowOp;
	}

	public void setShadowOp(ShadowOperation op)
	{
		this.shadowOp = op;
	}

	public long getLatency()
	{
		return this.latency;
	}

	public void beginTxn(long txnId)
	{
		this.txnId = txnId;
		this.hasBegun = true;
		this.timer.start();
	}

	public void endTxn()
	{
		this.timer.stop();
		this.latency = this.timer.getElapsedTime();
		this.hasEnded = true;
	}

	public void clear()
	{
		this.txnId = 0;
		this.latency = 0;
		this.hasBegun = false;
		this.hasEnded = false;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
	}

	public boolean hasBegun()
	{
		return this.hasBegun;
	}

	public boolean hasEnded()
	{
		return this.hasEnded;
	}
}