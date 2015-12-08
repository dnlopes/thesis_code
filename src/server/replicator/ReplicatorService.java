package server.replicator;


import server.agents.coordination.CoordinationAgent;
import server.agents.dispatcher.DispatcherAgent;
import server.agents.deliver.DeliverAgent;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.*;
import server.util.CompilePreparationException;
import server.util.TransactionCommitFailureException;

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
	public Status commitOperation(CRDTPreCompiledTransaction transaction) throws TException
	{
		try
		{
			return handleCommitOperation(transaction);
		} catch(CompilePreparationException e)
		{
			return new Status(false, e.getMessage());
		} catch(TransactionCommitFailureException e)
		{
			return new Status(false, e.getMessage());
		}
	}

	@Override
	public void sendToRemote(CRDTPreCompiledTransaction txn) throws TException
	{
		try
		{
			handleReceiveOperation(txn);
		} catch(TransactionCommitFailureException e)
		{
			LOG.warn(e.getMessage());
		}
	}

	@Override
	public void sendBatchToRemote(List<CRDTPreCompiledTransaction> batch) throws TException
	{
		try
		{
			handleReceiveBatch(batch);
		} catch(TransactionCommitFailureException e)
		{
			LOG.warn(e.getMessage());
		}
	}

	private Status handleCommitOperation(CRDTPreCompiledTransaction transaction)
			throws CompilePreparationException, TransactionCommitFailureException
	{
		transaction.setReplicatorId(this.replicatorId);

		this.coordAgent.handleCoordination(transaction);

		if(transaction.isReadyToCommit())
		{
			// replace symbols for real values and append prefixs when needed
			transaction.setTxnClock(this.replicator.getNextClock().getClockValue());
			this.replicator.prepareToCommit(transaction);
			transaction.setId(this.replicator.assignNewTransactionId());

			// wait for commit decision
			// if it suceeds, then dispatch this transaction to the dispatcher agent for later propagation

			Status localCommitStatus = this.replicator.commitOperation(transaction);

			if(localCommitStatus.isSuccess())
				this.dispatcher.dispatchTransaction(transaction);


			return localCommitStatus;
		} else
			return new Status(false, "txn was not ready for commit");
	}

	private void handleReceiveOperation(CRDTPreCompiledTransaction op) throws TransactionCommitFailureException
	{
		LOG.trace("received txn from other replicator");
		this.deliver.deliverTransaction(op);
	}

	private void handleReceiveBatch(List<CRDTPreCompiledTransaction> batch) throws TransactionCommitFailureException
	{
		LOG.info("received txn batch from remote node (size {})", batch.size());

		for(CRDTPreCompiledTransaction txn : batch)
			this.deliver.deliverTransaction(txn);
	}

}
