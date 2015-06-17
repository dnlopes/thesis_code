package nodes.replicator;


import nodes.Deliver;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.RuntimeUtils;
import runtime.operation.ShadowTransaction;
import util.defaults.Configuration;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;

/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);

	private Replicator replicator;
	private IReplicatorNetwork network;
	private Deliver deliver;

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.network = network;
		this.deliver = new CausalDeliver(this.replicator);
	}

	@Override
	public boolean commitOperation(ThriftOperation thriftOp)
	{
		//synchronized call
		LogicalClock newClock = this.replicator.getNextClock();

		if(Configuration.TRACE_ENABLED)
			LOG.trace("new clock assigned: {}", newClock.getClockValue());

		ShadowTransaction shadowTransaction = RuntimeUtils.decodeThriftOperation(thriftOp);
		shadowTransaction.setReplicatorId(this.replicator.getConfig().getId());
		shadowTransaction.setLogicalClock(newClock);

		thriftOp.setClock(shadowTransaction.getClock().getClockValue());
		thriftOp.setReplicatorId(shadowTransaction.getReplicatorId());

		// just deliver the operation to own replicator and wait for commit decision.
		// if it suceeds then async deliver the operation to other replicators
		boolean localCommit;
		localCommit = this.replicator.commitOperation(shadowTransaction);

		if(localCommit)
			network.sendOperationToRemote(thriftOp);

		return localCommit;
	}

	@Override
	public void commitOperationAsync(ThriftOperation thriftOp) throws TException
	{
		if(Configuration.TRACE_ENABLED)
			LOG.trace("received txn from other replicator");

		ShadowTransaction shadowTransaction = RuntimeUtils.decodeThriftOperation(thriftOp);
		LogicalClock remoteClock = new LogicalClock(thriftOp.getClock());
		shadowTransaction.setLogicalClock(remoteClock);
		shadowTransaction.setReplicatorId(thriftOp.getReplicatorId());

		this.deliver.dispatchOperation(shadowTransaction);
	}



}
