package server.agents.coordination;


import common.database.util.DatabaseMetadata;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import server.replicator.IReplicatorNetwork;
import server.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.Configuration;
import common.util.ExitCode;
import common.thrift.*;

import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 22/10/15.
 */
public class SimpleCoordinationAgent implements CoordinationAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(SimpleCoordinationAgent.class);
	private static final DatabaseMetadata metadata = Configuration.getInstance().getDatabaseMetadata();

	private final Replicator replicator;
	private final IDsManager idsManager;
	private final IReplicatorNetwork network;

	public SimpleCoordinationAgent(Replicator replicator)
	{
		this.replicator = replicator;
		this.network = this.replicator.getNetworkInterface();
		this.idsManager = new IDsManager(this.replicator.getPrefix(), this.replicator.getConfig());
	}

	@Override
	public void handleCoordination(CRDTTransaction transaction)
	{
		if(!transaction.isSetRequestToCoordinator())
		{
			transaction.setReadyToCommit(true);
			return;
		}

		CoordinatorRequest request = transaction.getRequestToCoordinator();
		CoordinatorResponse response = this.network.sendRequestToCoordinator(request);

		if(response.isSuccess())
		{
			handleCoordinatorResponse(response, transaction);
			transaction.setReadyToCommit(true);
		} else
		{
			transaction.setReadyToCommit(false);
			if(LOG.isWarnEnabled())
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
}
