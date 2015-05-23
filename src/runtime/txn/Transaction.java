package runtime.txn;


import database.util.FieldValue;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.Operation;
import runtime.operation.ShadowOperation;
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

	private static final Logger LOG_FILE = LoggerFactory.getLogger("txnLogger");

	private TransactionIdentifier txnId;
	private long latency;
	private StopWatch watch;
	private ShadowOperation shadowOp;
	private boolean readyToCommit;
	private List<Operation> txnOps;
	private Map<String, String> times;
	private Map<Integer, Operation> txnOpsMap;
	private int opsCounter;

	public Transaction(TransactionIdentifier txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.opsCounter = 0;
		this.shadowOp = null;
		this.watch = new StopWatch();
		this.readyToCommit = false;
		this.txnOps = new ArrayList<>();
		this.txnOpsMap = new HashMap<>();
		this.times = new HashMap<>();
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

	public ShadowOperation getShadowOp()
	{
		return this.shadowOp;
	}

	public long getLatency()
	{
		return this.latency;
	}

	public void startWatch()
	{
		this.watch.start();
	}

	public void finish()
	{
		this.watch.stop();
		this.latency = this.watch.getTime();
	}

	public void recordTime(String timeTag)
	{
		this.watch.stop();
		this.times.put(timeTag, String.valueOf(this.watch.getTime()));
		this.watch.reset();
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

	public boolean isReadOnly()
	{
		return this.txnOps.size() == 0;
	}

	public void printResults()
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("# runtime, coordination_time, commit_time\n");
		buffer.append(this.times.get("runtime"));
		buffer.append(",");
		buffer.append(this.times.get("coordination"));
		buffer.append(",");
		buffer.append(this.times.get("commit"));

		LOG_FILE.info(buffer.toString());
	}
}