package server.replicator;


import common.util.exception.InitComponentFailureException;
import common.util.exception.InvalidConfigurationException;
import common.util.exception.SocketConnectionException;
import server.agents.coordination.CoordinationAgent;
import server.agents.dispatcher.DispatcherAgent;
import server.agents.deliver.DeliverAgent;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.*;
import server.execution.TrxCompiler;
import server.util.CompilePreparationException;
import server.util.CoordinationFailureException;
import server.util.TransactionCommitFailureException;
import server.util.TransactionCompilationException;

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
	private final TrxCompiler compiler;
	private final int replicatorId;

	public ReplicatorService(Replicator replicator) throws InitComponentFailureException
	{
		this.replicator = replicator;
		this.replicatorId = replicator.getConfig().getId();
		this.deliver = this.replicator.getDeliver();
		this.dispatcher = this.replicator.getDispatcher();
		this.coordAgent = this.replicator.getCoordAgent();
		this.compiler = new TrxCompiler(replicator);
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
		} catch(CoordinationFailureException e)
		{
			return new Status(false, e.getMessage());
		} catch(SocketConnectionException e)
		{
			return new Status(false, e.getMessage());
		} catch(InvalidConfigurationException e)
		{
			return new Status(false, e.getMessage());
		} catch(TransactionCompilationException e)
		{
			return new Status(false, e.getMessage());
		}
	}

	@Override
	public void sendToRemote(CRDTCompiledTransaction txn) throws TException
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
	public void sendBatchToRemote(List<CRDTCompiledTransaction> batch) throws TException
	{
		try
		{
			handleReceiveBatch(batch);
		} catch(TransactionCommitFailureException e)
		{
			LOG.warn(e.getMessage());
		}
	}

	private Status handleCommitOperation(CRDTPreCompiledTransaction trx)
			throws CompilePreparationException, TransactionCommitFailureException, CoordinationFailureException,
			SocketConnectionException, InvalidConfigurationException, TransactionCompilationException
	{
		trx.setReplicatorId(replicatorId);

		coordAgent.handleCoordination(trx);

		if(trx.isReadyToCommit())
		{
			// finish trx compilation
			trx.setTxnClock(replicator.getNextClock().getClockValue());
			trx.setId(replicator.assignNewTransactionId());
			CRDTCompiledTransaction compiledTrx = compiler.compileTrx(trx);

			// wait for commit decision
			// if it suceeds, then dispatch this transaction to the dispatcher agent for later propagation
			Status commitStatus = replicator.commitOperation(compiledTrx);

			if(commitStatus.isSuccess())
				dispatcher.dispatchTransaction(compiledTrx);

			return commitStatus;
		} else
			return new Status(false, "txn was not ready for commit");
	}

	private void handleReceiveOperation(CRDTCompiledTransaction trx) throws TransactionCommitFailureException
	{
		LOG.trace("received txn from other replicator");
		this.deliver.deliverTransaction(trx);
	}

	private void handleReceiveBatch(List<CRDTCompiledTransaction> trxBatch) throws TransactionCommitFailureException
	{
		LOG.debug("received trx batch from remote node (size {})", trxBatch.size());

		for(CRDTCompiledTransaction txn : trxBatch)
			deliver.deliverTransaction(txn);
	}

}
