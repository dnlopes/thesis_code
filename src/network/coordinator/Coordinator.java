package network.coordinator;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.constraints.check.CheckConstraintEnforcer;
import database.constraints.unique.AutoIncrementConstraint;
import database.constraints.unique.AutoIncrementEnforcer;
import database.constraints.unique.UniqueConstraint;
import database.constraints.unique.UniqueConstraintEnforcer;
import database.util.DataField;
import database.util.DatabaseMetadata;
import database.util.DatabaseTable;
import network.AbstractNode;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.Configuration;
import util.thrift.RequestEntry;
import util.thrift.ResponseEntry;

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

	public List<ResponseEntry> processInvariants(List<RequestEntry> checkList)
	{
		List<ResponseEntry> results = new ArrayList<>();

		for(RequestEntry checkEntry : checkList)
		{
			ResponseEntry newResult = this.processRequestEntry(checkEntry);
			//if any of the requests failed we can return now and avoid processing the rest
			// either way, the txn on the other side will fail
			if(!newResult.isSuccess())
				return null;
		}

		LOG.trace("all requets were successfully processed");
		return results;
	}

	private ResponseEntry processRequestEntry(RequestEntry entry)
	{
		ResponseEntry newResult = new ResponseEntry();
		newResult.setId(entry.getId());
		newResult.setFieldName(entry.getFieldName());
		newResult.setTableName(entry.getTableName());

		String constraintId = entry.getConstraintId();
		switch(entry.getType())
		{
		case UNIQUE:
			String desiredValue = entry.getValue();
			if(this.uniquesEnforcers.get(constraintId).reservValue(desiredValue))
			{
				LOG.debug("new unique value reserved: {} for table-field {}", desiredValue, constraintId);
				newResult.setSuccess(true);
			} else
			{
				LOG.debug("unique value already in use {} for table-field {}", desiredValue, constraintId);
				newResult.setSuccess(false);
			}
			return newResult;
		case REQUEST_ID:
			int newId = this.autoIncrementsEnforcers.get(constraintId).getNextId();
			newResult.setResquestedValue(String.valueOf(newId));
			newResult.setSuccess(true);
			LOG.debug("providing new auto incremented value {} for table-field {}", newId, constraintId);
			return newResult;
		case APPLY_DELTA:
			String delta = entry.getValue();
			String pkValue = entry.getId();
			if(this.checkEnforcers.get(constraintId).applyDelta(pkValue, delta))
			{
				LOG.debug("delta value {} applied sucessfully", delta);
				newResult.setSuccess(true);
			} else
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
			Set<Constraint> invariants = table.getTableInvarists();

			for(Constraint constraint : invariants)
			{
				String constraintId = constraint.getConstraintIdentifier();
				List<DataField> fields = constraint.getFields();

				switch(constraint.getType())
				{
				case UNIQUE:
					UniqueConstraintEnforcer uniqueEnforcer = new UniqueConstraintEnforcer(fields, this.getConfig(),
							(UniqueConstraint) constraint);
					this.uniquesEnforcers.put(constraintId, uniqueEnforcer);
					break;
				case AUTO_INCREMENT:
					if(fields.size() > 1)
					{
						LOG.error("an auto increment constraint should only refer to one field");
						RuntimeHelper.throwRunTimeException("auto increment constriant references multiple " +
										"fields",
								ExitCode.INVALIDUSAGE);
					}
					AutoIncrementEnforcer autoIncrementEnforcer = new AutoIncrementEnforcer(fields.get(0),
							this.getConfig(), (AutoIncrementConstraint) constraint);
					this.autoIncrementsEnforcers.put(constraintId, autoIncrementEnforcer);
					break;
				case FOREIGN_KEY:
					LOG.warn("not yet implemented");
					break;
				case CHECK:
					if(fields.size() > 1)
					{
						LOG.error("a check constraint should only refer to one field");
						RuntimeHelper.throwRunTimeException("auto increment constriant references multiple " +
										"fields",
								ExitCode.INVALIDUSAGE);
					}
					CheckConstraintEnforcer checkEnforcer = new CheckConstraintEnforcer(fields.get(0),
							(CheckConstraint) constraint, this.getConfig());
					this.checkEnforcers.put(constraintId, checkEnforcer);
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
