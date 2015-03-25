package network.coordinator;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.constraints.check.CheckConstraintEnforcer;
import database.constraints.unique.AutoIncrementEnforcer;
import database.constraints.unique.UniqueConstraintEnforcer;
import database.util.DataField;
import database.util.DatabaseMetadata;
import database.util.DatabaseTable;
import network.AbstractNode;
import network.NodeMetadata;
import org.apache.thrift.transport.TTransportException;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
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

	private static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);
	private static final StopWatch watch = new LoggingStopWatch();

	private DatabaseMetadata databaseMetadata;

	// key is tableName-fieldName
	private Map<String, UniqueConstraintEnforcer> uniquesEnforcers;
	private Map<String, AutoIncrementEnforcer> autoIncrementsEnforcers;
	private Map<String, CheckConstraintEnforcer> checkEnforcers;

	private CoordinatorServerThread serverThread;

	public Coordinator(NodeMetadata nodeInfo)
	{
		super(nodeInfo);

		this.databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
		this.uniquesEnforcers = new HashMap<>();
		this.autoIncrementsEnforcers = new HashMap<>();
		this.checkEnforcers = new HashMap<>();
		watch.setTag("setup-coordinator");
		watch.start();
		this.setup();
		watch.stop();

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

	public List<ThriftCheckResult> processInvariants(List<ThriftCheckEntry> checkList)
	{
		List<ThriftCheckResult> results = new ArrayList<>();

		for(ThriftCheckEntry checkEntry : checkList)
		{
			ThriftCheckResult newResult = this.processRequestEntry(checkEntry);
			//if any of the requests failed we can return now and avoid processing the rest
			// either way, the txn on the other side will fail
			if(!newResult.isSuccess())
				return null;
		}

		LOG.trace("all requets were successfully processed");
		return results;
	}

	private ThriftCheckResult processRequestEntry(ThriftCheckEntry entry)
	{
		ThriftCheckResult newResult = new ThriftCheckResult();
		newResult.setId(entry.getId());

		String key = entry.getTableName() + "_" + entry.getFieldName();
		switch(entry.getType())
		{
		case UNIQUE:
			String desiredValue = entry.getValue();
			if(this.uniquesEnforcers.get(key).reserveValue(desiredValue))
				newResult.setSuccess(true);
			else
				newResult.setSuccess(false);
			return newResult;
		case REQUEST_ID:
			int newId = this.autoIncrementsEnforcers.get(key).getNextId();
			newResult.setResquestedValue(String.valueOf(newId));
			newResult.setSuccess(true);
			return newResult;
		case APPLY_DELTA:
			String delta = entry.getValue();
			int rowId = entry.getId();
			if(this.checkEnforcers.get(key).applyDelta(rowId, delta))
				newResult.setSuccess(true);
			else
				newResult.setSuccess(false);

			return newResult;
		case FOREIGN_KEY:
			LOG.warn("not yet implemented");
			break;
		default:
			LOG.warn("unexpected entry type");
			return null;
		}

		return newResult;
	}

	private void setup()
	{
		NodeMetadata replicatorMetadata = Configuration.getInstance().getReplicators().get(1);

		for(DatabaseTable table : this.databaseMetadata.getAllTables())
		{
			List<Constraint> invariants = table.getTableInvarists();

			for(Constraint constraint : invariants)
			{
				DataField field = constraint.getFields().get(0);
				String key = field.getTableName() + "_" + field.getFieldName();

				switch(constraint.getType())
				{
				case UNIQUE: //if is unique we now there is only one field..kind awfull hack though :(
					UniqueConstraintEnforcer uniqueEnforcer = new UniqueConstraintEnforcer(field, replicatorMetadata);
					this.uniquesEnforcers.put(key, uniqueEnforcer);
					break;
				case AUTO_INCREMENT:
					AutoIncrementEnforcer autoIncrementEnforcer = new AutoIncrementEnforcer(field, replicatorMetadata);
					this.autoIncrementsEnforcers.put(key, autoIncrementEnforcer);
					break;
				case FOREIGN_KEY:
					LOG.warn("not yet implemented");
					break;
				case CHECK:
					CheckConstraintEnforcer checkEnforcer = new CheckConstraintEnforcer(field,
							(CheckConstraint) constraint, replicatorMetadata);
					this.checkEnforcers.put(key, checkEnforcer);
					break;
				default:
					LOG.warn("unexpected constraint type");
				}
			}
		}
	}
}
