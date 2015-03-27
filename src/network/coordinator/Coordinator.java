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

	private static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);

	private DatabaseMetadata databaseMetadata;

	// key is tableName-fieldName
	private Map<String, UniqueConstraintEnforcer> uniquesEnforcers;
	private Map<String, AutoIncrementEnforcer> autoIncrementsEnforcers;
	private Map<String, CheckConstraintEnforcer> checkEnforcers;

	private CoordinatorServerThread serverThread;

	public Coordinator(CoordinatorConfig config)
	{
		super(config);

		this.databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
		this.uniquesEnforcers = new HashMap<>();
		this.autoIncrementsEnforcers = new HashMap<>();
		this.checkEnforcers = new HashMap<>();

		this.setup();

		try
		{
			this.serverThread = new CoordinatorServerThread(this);
			new Thread(this.serverThread).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on coordinator {}", this.getConfig().getName());
			e.printStackTrace();
		}

		LOG.info("coordinator {} online", this.config.getId());
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
			{
				LOG.debug("new unique value reserved: {} for table-field {}", desiredValue, key);
				newResult.setSuccess(true);
			}
			else
			{
				LOG.debug("unique value already in use {} for table-field {}", desiredValue, key);
				newResult.setSuccess(false);
			}
			return newResult;
		case REQUEST_ID:
			int newId = this.autoIncrementsEnforcers.get(key).getNextId();
			newResult.setResquestedValue(String.valueOf(newId));
			newResult.setFieldName(entry.getFieldName());
			newResult.setSuccess(true);
			LOG.debug("providing new auto incremented value {} for table-field {}", newId, key);
			return newResult;
		case APPLY_DELTA:
			String delta = entry.getValue();
			int rowId = entry.getId();
			if(this.checkEnforcers.get(key).applyDelta(rowId, delta))
			{
				LOG.debug("delta value {} applied sucessfully", delta);
				newResult.setSuccess(true);
			}
			else
			{
				LOG.debug("delta value {} not valid", delta);
				newResult.setSuccess(false);
			}
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
		for(DatabaseTable table : this.databaseMetadata.getAllTables())
		{
			List<Constraint> invariants = table.getTableInvarists();

			for(Constraint constraint : invariants)
			{
				DataField field = constraint.getFields().get(0);
				String key = field.getTableName() + "_" + field.getFieldName();

				switch(constraint.getType())
				{
				case UNIQUE:
					if(field.isAutoIncrement())
					{
						AutoIncrementEnforcer autoIncrementEnforcer = new AutoIncrementEnforcer(field,
								this.getConfig());
						this.autoIncrementsEnforcers.put(key, autoIncrementEnforcer);
					} else
					{
						UniqueConstraintEnforcer uniqueEnforcer = new UniqueConstraintEnforcer(field,
								this.getConfig());
						this.uniquesEnforcers.put(key, uniqueEnforcer);
					}
					break;
				case FOREIGN_KEY:
					LOG.warn("not yet implemented");
					break;
				case CHECK:
					CheckConstraintEnforcer checkEnforcer = new CheckConstraintEnforcer(field,
							(CheckConstraint) constraint, this.getConfig());
					this.checkEnforcers.put(key, checkEnforcer);
					break;
				default:
					LOG.warn("unexpected constraint type");
				}
			}
		}
	}

	public CoordinatorConfig getConfig()
	{
		return (CoordinatorConfig) this.config;
	}

}
