package runtime;


import database.util.FieldValue;
import runtime.operation.ShadowOperation;
import runtime.operation.ShadowTransaction;
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
	private ShadowTransaction shadowTransaction;
	private boolean readyToCommit;
	private List<ShadowOperation> shadowOperations;
	private Map<Integer, ShadowOperation> txnOpsMap;
	private int opsCounter;

	public Transaction(int txnId)
	{
		this.txnId = txnId;
		this.latency = 0;
		this.opsCounter = 0;
		this.shadowTransaction = null;
		this.readyToCommit = false;
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

	public ShadowTransaction getShadowTransaction()
	{
		return this.shadowTransaction;
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

	public List<ShadowOperation> getShadowOperations()
	{
		return this.shadowOperations;
	}

	public void updatedWithRequestedValues(List<RequestValue> reqValues)
	{
		for(RequestValue reqValue : reqValues)
		{
			ShadowOperation op = this.txnOpsMap.get(reqValue.getOpId());
			String requestedValue = reqValue.getRequestedValue();
			String fieldName = reqValue.getFieldName();
			op.getRow().updateFieldValue(new FieldValue(op.getRow().getTable().getField(fieldName), requestedValue));
		}
	}

	public void generateShadowTransaction()
	{
		List<String> shadowStatements = new ArrayList<>();

		for(ShadowOperation shadowOp : this.shadowOperations)
			shadowOp.generateStatements(shadowStatements);

		this.shadowTransaction = new ShadowTransaction(this.txnId, shadowStatements);
		this.readyToCommit = true;
	}

	public boolean isReadOnly()
	{
		return this.shadowOperations.size() == 0;
	}
}