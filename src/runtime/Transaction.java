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

	private static final Logger LOG = LoggerFactory.getLogger(Transaction.class);

	private TransactionId txnId;
	private long latency;
	private boolean hasBegun;

	private TimeStamp timestamp;
	private LogicalClock lc;
	private StopWatch timer;
	private ShadowOperation shadowOp;
	private String abortMessage;
	private boolean internalAborted;
	private boolean readOnly;
	private boolean readyToCommit;

	public Transaction()
	{
		this.txnId = new TransactionId(0);
		this.latency = 0;
		this.hasBegun = false;
		this.readOnly = true;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.timer = new StopWatch();
		this.internalAborted = false;
		this.readyToCommit = false;
	}

	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	public TransactionId getTxnId()
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

	public void start(long txnId)
	{
		this.txnId.setId(txnId);
		this.hasBegun = true;
		this.timer.start();
	}

	public void finish()
	{
		this.timer.stop();
		this.latency = this.timer.getElapsedTime();
	}

	public void resetState()
	{
		this.txnId.setId(0);
		this.timer.stop();
		this.latency = 0;
		this.hasBegun = false;
		this.shadowOp = null;
		this.timestamp = null;
		this.lc = null;
		this.abortMessage = null;
		this.readyToCommit = false;
	}

	public boolean hasBegun()
	{
		return this.hasBegun;
	}

	public boolean isInternalAborted()
	{
		return this.internalAborted;
	}

	public void setInternalAborted(String errorMessage)
	{
		this.internalAborted = true;
		this.abortMessage = errorMessage;
	}

	public String getAbortMessage()
	{
		return this.abortMessage;
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