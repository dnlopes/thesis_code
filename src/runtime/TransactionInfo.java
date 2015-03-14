package runtime;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.factory.TxnIdFactory;
import util.LogicalClock;
import util.TimeStamp;


/**
 * accumulates information on a transaction as it is processed
 */
public class TransactionInfo
{

	static final Logger LOG = LoggerFactory.getLogger(TransactionInfo.class);

	private long txnId;
	private long latency;
	private boolean hasBegun;
	private boolean hasEnded;
	private TimeStamp timestamp;
	private LogicalClock lc;
	private StopWatch timer;
	private Operation shadowOp;

	public TransactionInfo()
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

	public void beginTxn()
	{
		this.txnId = TxnIdFactory.getNextId();
		LOG.info("Beggining txn {}", this.txnId);

		this.hasBegun = true;
		this.timer.start();
	}

	public void endTxn()
	{
		this.timer.stop();
		this.latency = this.timer.getElapsedTime();
		this.hasEnded = true;
		LOG.info("Finished txn {}", this.txnId);
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