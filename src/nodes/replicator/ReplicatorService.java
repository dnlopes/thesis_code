package nodes.replicator;


import nodes.replicator.dispatcher.DispatcherAgent;
import nodes.replicator.dispatcher.AggregatorDispatcher;
import nodes.replicator.deliver.CausalDeliverAgent;
import nodes.replicator.deliver.DeliverAgent;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import util.thrift.*;

import java.util.List;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);

	private Replicator replicator;
	private DeliverAgent deliver;
	private DispatcherAgent dispatcher;

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.deliver = new CausalDeliverAgent(this.replicator);
		this.dispatcher = new AggregatorDispatcher(this.replicator.getNetworkInterface());
	}

	@Override
	public boolean commitOperation(CRDTTransaction transaction) throws TException
	{
		//synchronized call
		LogicalClock newClock = this.replicator.getNextClock();

		transaction.setReplicatorId(this.replicator.getConfig().getId());
		transaction.setTxnClock(newClock.getClockValue());

		if(LOG.isTraceEnabled())
			LOG.trace("new clock assigned: {}", newClock.getClockValue());

		//if we must coordinate then do it here. this is a blocking call
		if(transaction.isSetRequestToCoordinator())
			if(!this.coordinateOperation(transaction))
				return false;

		CRDTCompiledTransaction compiledTxn = ThriftUtils.compileCRDTTransaction(transaction);
		transaction.setCompiledTxn(compiledTxn);

		// just deliver the operation to own replicator and wait for commit decision.
		// if it suceeds then dispatch this transaction to the dispatcher agent for later propagation
		boolean localCommit = this.replicator.commitOperation(compiledTxn);

		if(localCommit)
			this.dispatcher.dispatchTransaction(transaction);

		return localCommit;
	}

	@Override
	public void commitOperationAsync(CRDTCompiledTransaction txn) throws TException
	{
		if(LOG.isTraceEnabled())
			LOG.trace("received txn from other replicator");

		this.deliver.deliverTransaction(txn);
	}

	private boolean coordinateOperation(CRDTTransaction txn)
	{
		//TODO implementation

		/*
		CoordinatorRequest request = txn.getRequestToCoordinator();
		CoordinatorResponse response = this.network.sendRequestToCoordinator(request);

		if(!response.isSuccess())
		{
			if(LOG.isTraceEnabled())
				LOG.trace("coordinator didnt allow txn to commit: {}", response.getErrorMessage());
			return false;
		}

		List<RequestValue> requestValues = response.getRequestedValues();

		if(requestValues != null && requestValues.size() > 0)
			this.updateShadowTransaction(txn, requestValues);

		return true;    */
		return true;
	}

	private void updateShadowTransaction(ThriftShadowTransaction shadowTransaction, List<RequestValue> reqValues)
	{
		for(RequestValue reqValue : reqValues)
		{
			String newValue = reqValue.getRequestedValue();
			int opId = reqValue.getOpId();

			String op = shadowTransaction.getOperations().get(opId);
			op = op.replaceAll(reqValue.getTempSymbol(), newValue);
			shadowTransaction.getOperations().put(opId, op);
		}
	}
}
