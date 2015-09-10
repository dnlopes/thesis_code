package nodes.replicator;


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
	private IReplicatorNetwork network;
	private DeliverAgent deliver;

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.network = network;
		this.deliver = new CausalDeliver(this.replicator);
	}

	@Override
	public boolean commitOperation(ThriftShadowTransaction shadowTransaction) throws TException
	{
		//if we must coordinate then do it here. this is a blocking call
		if(shadowTransaction.isSetRequestToCoordinator())
			if(!this.coordinateOperation(shadowTransaction))
				return false;

		shadowTransaction.setReplicatorId(this.replicator.getConfig().getId());

		//synchronized call
		LogicalClock newClock = this.replicator.getNextClock();

		if(LOG.isTraceEnabled())
			LOG.trace("new clock assigned: {}", newClock.getClockValue());

		shadowTransaction.setClock(newClock.getClockValue());

		// just deliver the operation to own replicator and wait for commit decision.
		// if it suceeds then async deliver the operation to other replicators
		boolean localCommit;
		localCommit = this.replicator.commitOperation(shadowTransaction);

		if(localCommit)
			network.sendOperationToRemote(shadowTransaction);

		return localCommit;
	}

	@Override
	public void commitOperationAsync(ThriftShadowTransaction shadowTransaction) throws TException
	{
		if(LOG.isTraceEnabled())
			LOG.trace("received txn from other replicator");

		this.deliver.dispatchOperation(shadowTransaction);
	}

	private boolean coordinateOperation(ThriftShadowTransaction shadowTransaction)
	{
		CoordinatorRequest request = shadowTransaction.getRequestToCoordinator();
		CoordinatorResponse response = this.network.sendRequestToCoordinator(request);

		if(!response.isSuccess())
		{
			if(LOG.isTraceEnabled())
				LOG.trace("coordinator didnt allow txn to commit: {}", response.getErrorMessage());
			return false;
		}

		List<RequestValue> requestValues = response.getRequestedValues();

		if(requestValues != null && requestValues.size() > 0)
			this.updateShadowTransaction(shadowTransaction, requestValues);

		return true;
	}

	private void updateShadowTransaction(ThriftShadowTransaction shadowTransaction, List<RequestValue> reqValues)
	{
		//TODO: is this correct?
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
