package runtime;


import database.util.FieldValue;
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
	private int txnId;
	private long latency;
	private ShadowOperation shadowOp;
	private boolean readyToCommit;
	private List<Operation> txnOps;
	private Map<Integer, Operation> txnOpsMap;
	private int opsCounter;

	public Transaction(int txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.opsCounter = 0;
		this.shadowOp = null;
		this.readyToCommit = false;
		this.txnOps = new ArrayList<>();
		this.txnOpsMap = new HashMap<>();
	}

	public void addOperation(Operation op)
	{
		this.txnOps.add(op);
		this.txnOpsMap.put(op.getOperationId(), op);
	}

	public int getTxnId()
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

		this.shadowOp = new ShadowOperation(this.txnId, shadowStatements);
		this.readyToCommit = true;
	}

	public boolean isReadOnly()
	{
		return this.txnOps.size() == 0;
	}
}