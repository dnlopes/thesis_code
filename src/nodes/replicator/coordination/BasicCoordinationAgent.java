package nodes.replicator.coordination;


import database.constraints.Constraint;
import database.constraints.unique.UniqueConstraint;
import database.util.DatabaseMetadata;
import database.util.table.DatabaseTable;
import nodes.replicator.IReplicatorNetwork;
import nodes.replicator.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.Configuration;
import util.ExitCode;
import util.thrift.*;

import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 22/10/15.
 */
public class BasicCoordinationAgent implements CoordinationAgent
{

	private static final Logger LOG = LoggerFactory.getLogger(BasicCoordinationAgent.class);

	private final Replicator replicator;
	private final IReplicatorNetwork network;
	private final DatabaseMetadata metadata;

	public BasicCoordinationAgent(Replicator replicator)
	{
		this.replicator = replicator;
		this.network = this.replicator.getNetworkInterface();
		this.metadata = Configuration.getInstance().getDatabaseMetadata();
	}

	@Override
	public void handleCoordination(CRDTTransaction transaction)
	{
		//TODO implementation
		if(transaction.isSetRequestToCoordinator())
		{
			CoordinatorRequest request = transaction.getRequestToCoordinator();
			CoordinatorResponse response = this.network.sendRequestToCoordinator(request);
			handleCoordinatorResponse(response, transaction);

			if(!transaction.isReadyToCommit())
			{
				if(LOG.isTraceEnabled())
					LOG.trace("coordinator didnt allow txn to commit: {}", response.getErrorMessage());
			}
		} else
			transaction.setReadyToCommit(true);
	}

	private void handleCoordinatorResponse(CoordinatorResponse response, CRDTTransaction transaction)
	{
		if(response.isSuccess())
			transaction.setReadyToCommit(true);
		else
		{
			transaction.setReadyToCommit(false);
			return;
		}

		if(response.isSetRequestedValues())
		{
			List<RequestValue> requestedValues = response.getRequestedValues();
			replaceSymbolsForValues(requestedValues, transaction);
		}
	}

	private void replaceSymbolsForValues(List<RequestValue> requestedValues, CRDTTransaction transaction)
	{
		Map<String, SymbolEntry> symbols = transaction.getSymbolsMap();

		for(RequestValue requestValue : requestedValues)
		{
			SymbolEntry symbolEntry = symbols.get(requestValue.getTempSymbol());
			symbolEntry.setRealValue(requestValue.getRequestedValue());
		}
	}

	private CoordinatorRequest createRequest(CRDTTransaction transaction)
	{
		CoordinatorRequest request = new CoordinatorRequest();

		for(CRDTOperation op : transaction.getOpsList())
		{
			DatabaseTable dbTable = this.metadata.getTable(op.getTableName());

			switch(op.getOpType())
			{
			case INSERT:
			case INSERT_CHILD:
				for(Constraint c : dbTable.getTableInvarists())
				{
					if(!c.requiresCoordination())
						continue;
					switch(c.getType())
					{
					case UNIQUE:
						String unique = this.createUniqueValue((UniqueConstraint) c, op.getNewFieldValues());
						UniqueValue uniqueValue = new UniqueValue(c.getConstraintIdentifier(), unique);
						request.addToUniqueValues(uniqueValue);
						break;
					case CHECK:
						break;
					case FOREIGN_KEY:
						break;
					default:
						RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
					}
				}

			case UPDATE:
			case UPDATE_CHILD:
				for(Constraint c : dbTable.getTableInvarists())
				{
					if(!c.requiresCoordination())
						continue;

					switch(c.getType())
					{
					case UNIQUE:
						Map<String, String> touchedFields = op.getNewFieldValues();
						break;
					case CHECK:
						//TODO
						break;
					case FOREIGN_KEY:
						break;
					default:
						RuntimeUtils.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
					}
				}
				break;
			default:
				break;
			}
		}

		return request;
	}

	private String createUniqueValue(UniqueConstraint constraint, Map<String, String> fieldsValuesMap)
	{
		return null;
	}
}
