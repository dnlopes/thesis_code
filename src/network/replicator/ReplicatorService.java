package network.replicator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.Utils;
import runtime.operation.ShadowOperation;
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

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.network = network;
	}

	@Override
	public boolean commitOperation(ThriftOperation thriftOp)
	{
		//synchronized call
		LogicalClock newClock = this.replicator.getNextClock();
		LOG.info("new clock assigned: {}", newClock.getClockValue());

		ShadowOperation shadowOp = Utils.decodeThriftOperation(thriftOp);
		shadowOp.setReplicatorId(this.replicator.getConfig().getId());
		shadowOp.setLogicalClock(newClock);

		thriftOp.setClock(shadowOp.getClock().getClockValue());
		thriftOp.setReplicatorId(shadowOp.getReplicatorId());

		// just deliver the operation to own replicator and wait for commit decision.
		// if it suceeds then async deliver the operation to other replicators
		boolean localCommit;
		localCommit = this.replicator.commitOperation(shadowOp);

		if(localCommit)
			network.sendOperationToRemote(thriftOp);

		return localCommit;
	}

	@Override
	public void commitOperationAsync(ThriftOperation shadowOp) throws TException
	{
		LOG.debug("received op from other replicator");

		ShadowOperation decodedOp = Utils.decodeThriftOperation(shadowOp);
		LogicalClock remoteClock = new LogicalClock(shadowOp.getClock());
		decodedOp.setLogicalClock(remoteClock);
		decodedOp.setReplicatorId(shadowOp.getReplicatorId());

		//synchronized
		this.replicator.mergeWithRemoteClock(remoteClock);
		this.replicator.commitOperation(decodedOp);
	}
}
