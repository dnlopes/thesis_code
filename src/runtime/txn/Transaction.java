package runtime.txn;


import org.perf4j.StopWatch;
import runtime.operation.Operation;
import runtime.operation.ShadowOperation;
import util.LogicalClock;
import util.TimeStamp;

import java.util.ArrayList;
import java.util.List;


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
	private boolean readyToCommit;
	private List<Operation> txnOps;

	public Transaction(TransactionIdentifier txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.timer = new StopWatch();
		this.readyToCommit = false;
		this.txnOps = new ArrayList<>();
	}

	public void addOperation(Operation op)
	{
		this.txnOps.add(op);
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

	public boolean isReadyToCommit()
	{
		return this.readyToCommit;
	}
}