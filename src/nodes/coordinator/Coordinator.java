package nodes.coordinator;


import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.constraints.check.CheckConstraintEnforcer;
import database.constraints.unique.AutoIncrementConstraint;
import database.constraints.unique.AutoIncrementEnforcer;
import database.constraints.unique.UniqueConstraint;
import database.constraints.unique.UniqueConstraintEnforcer;
import database.util.field.DataField;
import database.util.DatabaseMetadata;
import database.util.table.DatabaseTable;
import nodes.AbstractNode;
import nodes.NodeConfig;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;
import util.thrift.*;

import java.util.*;


/**
 * Created by dnlopes on 22/03/15.
 */
public class Coordinator extends AbstractNode
{

	private static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);

	private DatabaseMetadata databaseMetadata;
	private CoordinatorServerThread serverThread;

	// key is tableName-fieldName
	private Map<String, UniqueConstraintEnforcer> uniquesEnforcers;
	private Map<String, AutoIncrementEnforcer> autoIncrementsEnforcers;
	private Map<String, CheckConstraintEnforcer> checkEnforcers;

	public Coordinator(NodeConfig config)
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
			RuntimeUtils.throwRunTimeException("coordinator server thread initialization failed",
					ExitCode.NOINITIALIZATION);
		}

		System.out.println("coordinator " + this.config.getId() + " online");
	}

	public CoordinatorResponse processRequest(Request request)
	{
		CoordinatorResponse response = new CoordinatorResponse();
		List<RequestValue> requestedValues = new ArrayList<>();

		for(RequestUnit unit : request.getRequests())
		{
			boolean res = false;

			if(unit.isSetApplyDelta())
				res = processApplyDelta(response, unit.getApplyDelta());

			if(unit.isSetUniqueValue())
				res = processUniqueValue(response, unit.getUniqueValue());

			if(unit.isSetRequestValue())
			{
				this.processRequestValue(unit.getRequestValue());
				requestedValues.add(unit.getRequestValue());
			}

			if(!res)
			{
				response.setSuccess(false);
				return response;
			}
		}

		response.setRequestedValues(requestedValues);

		if(LOG.isTraceEnabled())
			LOG.trace("all requests were successfully processed");

		response.setSuccess(true);
		return response;
	}

	private boolean processApplyDelta(CoordinatorResponse response, ApplyDelta applyDelta)
	{
		String delta = applyDelta.getDeltaValue();
		String rowId = applyDelta.getRowId();
		String constraintId = applyDelta.getConstraintId();

		if(this.checkEnforcers.get(constraintId).applyDelta(rowId, delta))
		{
			if(LOG.isTraceEnabled())
				LOG.trace("delta value {} applied sucessfully", delta);
			return true;
		} else
		{
			String error = "delta value " + delta + " not valid for constraint " + constraintId + " in row " + rowId;
			response.setErrorMessage(error);
			LOG.warn(error);
			return false;
		}
	}

	private boolean processUniqueValue(CoordinatorResponse response, UniqueValue uniqueValue)
	{
		String desiredValue = uniqueValue.getValue();
		String constraintId = uniqueValue.getConstraintId();
		UniqueConstraintEnforcer enforcer = this.uniquesEnforcers.get(constraintId);

		if(enforcer.reservValue(desiredValue))
		{
			if(LOG.isTraceEnabled())
				LOG.trace("new unique value reserved: {} for table-field {}", desiredValue, constraintId);

			return true;
		} else
		{
			String error = "unique value already in use: " + desiredValue + " for table " + enforcer.getTableName();
			response.setErrorMessage(error);
			if(LOG.isTraceEnabled())
				LOG.trace("unique value already in use {} for table-field {}", desiredValue, constraintId);

			return false;
		}
	}

	private boolean processRequestValue(RequestValue reqValue)
	{
		String constraintId = reqValue.getConstraintId();
		int newId = this.autoIncrementsEnforcers.get(constraintId).getNextId();
		reqValue.setRequestedValue(String.valueOf(newId));
		if(LOG.isTraceEnabled())
			LOG.trace("providing new auto incremented value {} for table-field {}", newId, constraintId);

		return true;
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
					UniqueConstraintEnforcer uniqueEnforcer = new UniqueConstraintEnforcer(fields, this.config,
							(UniqueConstraint) constraint);
					this.uniquesEnforcers.put(constraintId, uniqueEnforcer);
					break;
				case AUTO_INCREMENT:
					if(fields.size() > 1)
					{
						LOG.error("an auto increment constraint should only refer to one field");
						RuntimeUtils.throwRunTimeException("auto increment constriant references multiple " + "fields",
								ExitCode.INVALIDUSAGE);
					}
					AutoIncrementEnforcer autoIncrementEnforcer = new AutoIncrementEnforcer(fields.get(0),
							this.config, (AutoIncrementConstraint) constraint);
					this.autoIncrementsEnforcers.put(constraintId, autoIncrementEnforcer);
					break;
				case FOREIGN_KEY:
					break;
				case CHECK:
					if(fields.size() > 1)
					{
						LOG.error("a check constraint should only refer to one field");
						RuntimeUtils.throwRunTimeException("auto increment constriant references multiple " + "fields",
								ExitCode.INVALIDUSAGE);
					}
					CheckConstraintEnforcer checkEnforcer = new CheckConstraintEnforcer(fields.get(0),
							(CheckConstraint) constraint, this.config);
					this.checkEnforcers.put(constraintId, checkEnforcer);
					break;
				default:
					LOG.warn("unexpected constraint type");
				}
			}
		}
	}
}
