package runtime;


import runtime.operation.ShadowOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Accumulates information on a transaction as it is processed
 */
public class Transaction
{

	private int txnId;
	private long latency;
	private List<ShadowOperation> shadowOperations;
	private Map<Integer, ShadowOperation> txnOpsMap;
	private int opsCounter;

	public Transaction(int txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.opsCounter = 0;
		this.shadowOperations = new ArrayList<>();
		this.txnOpsMap = new HashMap<>();
	}

	public void addOperation(ShadowOperation op)
	{
		this.shadowOperations.add(op);
		this.txnOpsMap.put(op.getOperationId(), op);
	}

	public int getTxnId()
	{
		return this.txnId;
	}

	public long getLatency()
	{
		return this.latency;
	}

	public int getNextOperationId()
	{
		return this.opsCounter++;
	}

	public List<ShadowOperation> getShadowOperations()
	{
		return this.shadowOperations;
	}

	public boolean isReadOnly()
	{
		return this.shadowOperations.size() == 0;
	}
}