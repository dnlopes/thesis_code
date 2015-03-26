package runtime.txn;


import org.perf4j.StopWatch;
import runtime.operation.ShadowOperation;
import util.LogicalClock;
import util.TimeStamp;


/**
 * accumulates information on a transaction as it is processed
 */
public class Transaction
{

	private TransactionIdentifier txnId;
	private long latency;

	private TimeStamp timestamp;
	private LogicalClock lc;
	private StopWatch timer;
	private ShadowOperation shadowOp;
	private boolean readOnly;
	private boolean readyToCommit;

	public Transaction(TransactionIdentifier txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.readOnly = true;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.timer = new StopWatch();
		this.readyToCommit = false;
	}

	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	public TransactionIdentifier getTxnId()
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

	public long getLatency()
	{
		return this.latency;
	}

	public void start()
	{
		this.timer.start();
	}

	public void finish()
	{
		this.timer.stop();
		this.latency = this.timer.getElapsedTime();
	}

	public void resetState()
	{
		this.txnId.resetValue();
		this.timer.stop();
		this.latency = 0;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.readyToCommit = false;
	}

	/**
	 * Called before commit.
	 * This method looks at the txn write set and generates a minimal sequence of sql operations
	 * to apply the intended changes
	 */
	public void setReadyToCommit(ShadowOperation shadowOp)
	{
		this.shadowOp = shadowOp;
		this.readyToCommit = true;
	}

	public void setNotReadOnly()
	{
		this.readOnly = false;
	}

	public boolean isReadyToCommit()
	{
		return this.readyToCommit;
	}
}