package runtime.txn;


import database.util.FieldValue;
import org.perf4j.StopWatch;
import runtime.operation.Operation;
import runtime.operation.ShadowOperation;
import util.TimeStamp;
import util.thrift.RequestValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * accumulates information on a transaction as it is processed
 */
public class Transaction
{

	private TransactionIdentifier txnId;
	private long latency;

	private TimeStamp timestamp;
	private StopWatch timer;
	private ShadowOperation shadowOp;
	private boolean readyToCommit;
	private List<Operation> txnOps;
	private Map<Integer, Operation> txnOpsMap;
	private int opsCounter;

	public Transaction(TransactionIdentifier txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.opsCounter = 0;
		this.shadowOp = null;
		this.timestamp = null;
		this.timer = new StopWatch();
		this.readyToCommit = false;
		this.txnOps = new ArrayList<>();
		this.txnOpsMap = new HashMap<>();
	}

	public void addOperation(Operation op)
	{
		this.txnOps.add(op);
		this.txnOpsMap.put(op.getOperationId(), op);
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

	public boolean isReadyToCommit()
	{
		return this.readyToCommit;
	}

	public int getNextOperationId()
	{
		return this.opsCounter++;
	}

	public List<Operation> getTxnOps()
	{
		return this.txnOps;
	}

	public void updatedWithRequestedValues(List<RequestValue> reqValues)
	{
		for(RequestValue reqValue : reqValues)
		{
			Operation op = this.txnOpsMap.get(reqValue.getOpId());
			String requestedValue = reqValue.getRequestedValue();
			String fieldName = reqValue.getFieldName();
			op.getRow().updateFieldValue(new FieldValue(op.getRow().getTable().getField(fieldName), requestedValue));
		}
	}

	public void generateShadowOperation()
	{
		List<String> shadowStatements = new ArrayList<>();

		for(Operation op : this.txnOps)
			op.generateOperationStatements(shadowStatements);

		this.shadowOp = new ShadowOperation(this.txnId.getValue(), shadowStatements);
		this.readyToCommit = true;
	}
}