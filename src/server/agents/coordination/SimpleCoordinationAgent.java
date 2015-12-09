package server.agents.coordination;


import common.util.Environment;
import common.util.defaults.ReplicatorDefaults;
import common.util.exception.InitComponentFailureException;
import server.replicator.IReplicatorNetwork;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.*;
import server.util.CompilePreparationException;
import server.util.CoordinationFailureException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 22/10/15.
 */
public class SimpleCoordinationAgent implements CoordinationAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(SimpleCoordinationAgent.class);

	private Replicator replicator;
	private IReplicatorNetwork network;
	private AtomicInteger sentRequestsCounter;
	private ScheduledExecutorService scheduleService;

	public SimpleCoordinationAgent(Replicator replicator) throws InitComponentFailureException
	{
		if(Environment.IS_ZOOKEEPER_REQUIRED)
		{
			this.sentRequestsCounter = new AtomicInteger();
			this.replicator = replicator;
			this.network = this.replicator.getNetworkInterface();

			this.scheduleService = Executors.newScheduledThreadPool(1);
			this.scheduleService.scheduleAtFixedRate(new StateChecker(), ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL * 4,
					ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL, TimeUnit.MILLISECONDS);
		}
		else
			LOG.info("zookeeper is not required in this environment");
	}

	@Override
	public void handleCoordination(CRDTPreCompiledTransaction transaction)
			throws CompilePreparationException, CoordinationFailureException
	{
		if(!transaction.isSetRequestToCoordinator())
		{
			transaction.setReadyToCommit(true);
			return;
		}

		if(!Environment.IS_ZOOKEEPER_REQUIRED)
			throw new CoordinationFailureException("current environment does not require coordination but proxy " +
					"generated a request");

		this.sentRequestsCounter.incrementAndGet();

		CoordinatorRequest request = transaction.getRequestToCoordinator();
		long beginTime = System.nanoTime();
		CoordinatorResponse response = this.network.sendRequestToCoordinator(request);
		long estimatedTime = System.nanoTime() - beginTime;
		double estimatedtime_double = estimatedTime * 0.000001;
		System.out.println("coordination time: " + estimatedtime_double);

		if(response.isSuccess())
		{
			handleCoordinatorResponse(response, transaction);
			transaction.setReadyToCommit(true);
		} else
		{
			transaction.setReadyToCommit(false);
			LOG.warn("coordinator didnt allow txn to commit: {}", response.getErrorMessage());
		}
	}

	private void handleCoordinatorResponse(CoordinatorResponse response, CRDTPreCompiledTransaction transaction)
			throws CompilePreparationException
	{
		if(response.isSetRequestedValues())
		{
			List<RequestValue> requestedValues = response.getRequestedValues();
			replaceSymbolsForValues(requestedValues, transaction);
		}
	}

	private void replaceSymbolsForValues(List<RequestValue> requestedValues, CRDTPreCompiledTransaction transaction)
			throws CompilePreparationException
	{
		Map<String, SymbolEntry> symbols = transaction.getSymbolsMap();

		// lets replace the symbols for the values received from coordinator
		for(RequestValue requestValue : requestedValues)
		{
			if(requestValue.isSetRequestedValue())
			{
				SymbolEntry symbolEntry = symbols.get(requestValue.getTempSymbol());
				symbolEntry.setRealValue(requestValue.getRequestedValue());
			}
		}
	}

	private class StateChecker implements Runnable
	{

		private int id = replicator.getConfig().getId();

		@Override
		public void run()
		{
			LOG.info("<r{}> number of coordination events: {}", id, sentRequestsCounter.get());
		}
	}
}
