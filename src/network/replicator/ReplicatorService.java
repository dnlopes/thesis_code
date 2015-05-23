package network.replicator;


import network.Deliver;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.LogicalClock;
import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);
	private static final int CAUSAL_DELIVERY_UPDATE_INTERVAL = 3;

	private Replicator replicator;
	private IReplicatorNetwork network;
	private Deliver deliver;

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.network = network;
		this.deliver = new CausalDeliver(this.replicator);

		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(this.deliver, 0, CAUSAL_DELIVERY_UPDATE_INTERVAL, TimeUnit.SECONDS);
	}

	@Override
	public boolean commitOperation(ThriftOperation thriftOp)
	{
		//synchronized call
		LogicalClock newClock = this.replicator.getNextClock();
		LOG.trace("new clock assigned: {}", newClock.getClockValue());

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
	public void commitOperationAsync(ThriftOperation thriftOp) throws TException
	{
		LOG.trace("received op from other replicator");

		ShadowOperation shadowOp = Utils.decodeThriftOperation(thriftOp);
		LogicalClock remoteClock = new LogicalClock(thriftOp.getClock());
		shadowOp.setLogicalClock(remoteClock);
		shadowOp.setReplicatorId(thriftOp.getReplicatorId());

		this.deliver.dispatchOperation(shadowOp);
	}



}
