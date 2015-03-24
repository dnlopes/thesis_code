package network.coordinator;


import database.constraints.Constraint;
import database.util.DataField;
import database.util.DatabaseMetadata;
import database.util.DatabaseTable;
import network.AbstractNode;
import network.NodeMetadata;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import util.thrift.ThriftCheckEntry;
import util.thrift.ThriftCheckResult;

import java.util.*;


/**
 * Created by dnlopes on 22/03/15.
 */
public class Coordinator extends AbstractNode
{

	static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);

	private DatabaseMetadata databaseMetadata;
	private List<Constraint> invariants;

	// key is tableName-fieldName
	private Map<String, Set<String>> uniques;
	private Map<String, Long> autoIncremented;

	private CoordinatorServerThread serverThread;


	public Coordinator(NodeMetadata nodeInfo)
	{
		super(nodeInfo);

		this.databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
		this.uniques = new HashMap<>();
		this.autoIncremented = new HashMap<>();
		this.invariants = new ArrayList<>();
		this.setup();

		try
		{
			this.serverThread = new CoordinatorServerThread(this);
			new Thread(this.serverThread).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on coordinator {}", this.getName());
			e.printStackTrace();
		}
	}

	public void processInvariants(List<ThriftCheckEntry> checkList)
	{
		List<ThriftCheckResult> results = new ArrayList<>();
		boolean requestSuccess = true;

		for(ThriftCheckEntry checkEntry : checkList)
		{
			ThriftCheckResult newResult = this.processRequestEntry(checkEntry);
		}

		LOG.trace("processing invariants list");
	}

	private ThriftCheckResult processRequestEntry(ThriftCheckEntry entry)
	{
		ThriftCheckResult newResult = new ThriftCheckResult();
		newResult.setId(entry.getId());
		newResult.setType(entry.getType());

		switch(entry.getType())
		{
		case UNIQUE:
			String desiredValue = entry.getValue();
			String table = entry.getTableName();

			if(this.reservUniqueValue(table, desiredValue))
				newResult.setSuccess(true);
			else
				newResult.setSuccess(false);

			return newResult;
		case DELTA:
		case GREATHER_THAN:
		case LESSER_THAN:
		case REQUEST_VALUE:
		case FOREIGN_KEY:
			break;
		default:
			LOG.warn("unexpected entry type");
			return null;
		}

		return newResult;
	}

	private boolean reservUniqueValue(String tableName, String value)
	{
		Set<String> uniqueValues = this.uniques.get(tableName);
		return uniqueValues.add(value);
	}


	private void setup()
	{
		for(DatabaseTable table: this.databaseMetadata.getAllTables())
		{
			List<Constraint> invariants = table.getTableInvarists();

			this.invariants.addAll(invariants);

			for(Constraint inv: this.invariants)
			{
				switch(inv.getType())
				{
				case UNIQUE: //if is unique we now there is only one field..kind awfull hack though :(
					DataField uniqueField = inv.getFields().get(0);
					String key = uniqueField.getTableName() + "-" + uniqueField.getFieldName();
					Set<String> newSet = new HashSet<>();
					this.uniques.put(key, newSet);
					break;
				case FOREIGN_KEY:
					LOG.warn("not yet implemented");
					break;
				case CHECK:
					return;
				default:
					LOG.warn("unexpected constraint type");
				}
			}
		}
	}
}
