package server.agents.coordination;


import common.database.util.DatabaseMetadata;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.util.Environment;
import common.util.defaults.ReplicatorDefaults;
import server.replicator.IReplicatorNetwork;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.util.ExitCode;
import common.thrift.*;

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
	private static final DatabaseMetadata metadata = Environment.DB_METADATA;

	private final Replicator replicator;
	private final IDsManager idsManager;
	private final IReplicatorNetwork network;
	private AtomicInteger sentRequestsCounter;
	private final ScheduledExecutorService scheduleService;

	public SimpleCoordinationAgent(Replicator replicator)
	{
		this.sentRequestsCounter = new AtomicInteger();
		this.replicator = replicator;
		this.network = this.replicator.getNetworkInterface();
		this.idsManager = new IDsManager(this.replicator.getPrefix(), this.replicator.getConfig());

		this.scheduleService = Executors.newScheduledThreadPool(1);
		this.scheduleService.scheduleAtFixedRate(new StateChecker(), ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL*4,
				ReplicatorDefaults.STATE_CHECKER_THREAD_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void handleCoordination(CRDTTransaction transaction)
	{
		if(!transaction.isSetRequestToCoordinator())
		{
			transaction.setReadyToCommit(true);
			return;
		}

		this.sentRequestsCounter.incrementAndGet();

		CoordinatorRequest request = transaction.getRequestToCoordinator();
		CoordinatorResponse response = this.network.sendRequestToCoordinator(request);

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

	private void handleCoordinatorResponse(CoordinatorResponse response, CRDTTransaction transaction)
	{
		if(response.isSetRequestedValues())
		{
			List<RequestValue> requestedValues = response.getRequestedValues();
			replaceSymbolsForValues(requestedValues, transaction);
		}
	}

	private void replaceSymbolsForValues(List<RequestValue> requestedValues, CRDTTransaction transaction)
	{
		Map<String, SymbolEntry> symbols = transaction.getSymbolsMap();

		// first lets replace the symbols for the values received from coordinator
		for(RequestValue requestValue : requestedValues)
		{
			if(requestValue.isSetRequestedValue())
			{
				SymbolEntry symbolEntry = symbols.get(requestValue.getTempSymbol());
				symbolEntry.setRealValue(requestValue.getRequestedValue());
			}
		}

		// then, lets generate unique values locally
		for(SymbolEntry symbolEntry : symbols.values())
		{
			// already got value from coordinator
			if(symbolEntry.isSetRealValue())
				continue;

			// lets generate a unique value locally
			DatabaseTable dbTable = metadata.getTable(symbolEntry.getTableName());
			DataField dataField = dbTable.getField(symbolEntry.getFieldName());

			if(dataField.isNumberField())
				symbolEntry.setRealValue(String.valueOf(
						this.idsManager.getNextId(symbolEntry.getTableName(), symbolEntry.getFieldName())));
			else
				RuntimeUtils.throwRunTimeException("unexpected datafield type", ExitCode.INVALIDUSAGE);
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
