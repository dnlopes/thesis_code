package server.replicator;


import common.thrift.ThriftUtils;
import server.agents.coordination.CoordinationAgent;
import server.agents.dispatcher.DispatcherAgent;
import server.agents.deliver.DeliverAgent;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.LogicalClock;
import common.thrift.*;

import java.util.List;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);

	private final Replicator replicator;
	private final DeliverAgent deliver;
	private final DispatcherAgent dispatcher;
	private final CoordinationAgent coordAgent;
	private final int replicatorId;

	public ReplicatorService(Replicator replicator)
	{
		this.replicator = replicator;
		this.replicatorId = replicator.getConfig().getId();
		this.deliver = this.replicator.getDeliver();
		this.dispatcher = this.replicator.getDispatcher();
		this.coordAgent = this.replicator.getCoordAgent();
	}

	@Override
	public boolean commitOperation(CRDTTransaction transaction) throws TException
	{
		return handleCommitOperation(transaction);
	}

	@Override
	public void sendToRemote(CRDTCompiledTransaction txn) throws TException
	{
		handleReceiveOperation(txn);
	}

	@Override
	public void sendBatchToRemote(List<CRDTCompiledTransaction> batch) throws TException
	{
		handleReceiveBatch(batch);
	}

	private boolean handleCommitOperation(CRDTTransaction transaction)
	{
		LogicalClock newClock = this.replicator.getNextClock();

		transaction.setReplicatorId(this.replicatorId);
		transaction.setTxnClock(newClock.getClockValue());

		this.coordAgent.handleCoordination(transaction);

		if(transaction.isReadyToCommit())
		{
			// replace symbols for real values and append prefixs when needed
			this.replicator.prepareToCommit(transaction);
			CRDTCompiledTransaction compiledTxn = ThriftUtils.compileCRDTTransaction(transaction);
			transaction.setCompiledTxn(compiledTxn);

			// wait for commit decision
			// if it suceeds, then dispatch this transaction to the dispatcher agent for later propagation
			boolean localCommit = this.replicator.commitOperation(compiledTxn);

			if(localCommit)
				this.dispatcher.dispatchTransaction(transaction);

			return localCommit;
		} else
			return false;
	}

	private void handleReceiveOperation(CRDTCompiledTransaction op)
	{
		if(LOG.isTraceEnabled())
			LOG.trace("received txn from other replicator");

		this.deliver.deliverTransaction(op);
	}

	private void handleReceiveBatch(List<CRDTCompiledTransaction> batch)
	{
		LOG.info("received txn batch from remote node (size {})", batch.size());

		for(CRDTCompiledTransaction txn : batch)
			this.deliver.deliverTransaction(txn);
	}

}
