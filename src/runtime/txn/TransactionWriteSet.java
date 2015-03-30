package runtime.txn;


import database.constraints.*;
import database.constraints.check.CheckConstraint;
import database.util.DataField;
import database.util.DatabaseTable;
import database.util.PrimaryKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.Configuration;
import util.thrift.RequestEntry;
import util.thrift.RequestType;
import util.thrift.ResponseEntry;

import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TransactionWriteSet
{

	private static final Logger LOG = LoggerFactory.getLogger(TransactionWriteSet.class);

	private Set<String> treatedConstraints;

	private Map<String, TableWriteSet> txnWriteSet;
	private List<String> statements;

	// list of tuples that will be modified after contacting the coordinator
	// for instance when requesting a new id
	private Map<PrimaryKeyValue, TupleWriteSet> modifiedTuples;

	public TransactionWriteSet()
	{
		this.txnWriteSet = new HashMap<>();
		this.statements = new ArrayList<>();
		this.modifiedTuples = new HashMap<>();
		this.treatedConstraints = new HashSet<>();
	}

	public void resetWriteSet()
	{
		for(TableWriteSet tableSet : this.txnWriteSet.values())
			tableSet.reset();

		this.txnWriteSet.clear();
		this.statements.clear();
		this.modifiedTuples.clear();
		this.treatedConstraints.clear();
	}

	public void addTableWriteSet(String tableName, TableWriteSet writeSet)
	{
		this.txnWriteSet.put(tableName, writeSet);
	}

	public void generateMinimalStatements() throws SQLException
	{
		List<String> allStatements = new ArrayList<>();
		this.generateDeletes(allStatements);
		this.generateUpdates(allStatements);
		this.generateInserts(allStatements);

		this.statements = allStatements;
	}

	public List<String> getStatements()
	{
		return this.statements;
	}

	private void generateUpdates(List<String> allStatements) throws SQLException
	{
		for(TableWriteSet tableWriteSet : this.txnWriteSet.values())
			tableWriteSet.generateUpdateStatements(allStatements);
	}

	/**
	 * Generates all delete statements for a given transaction
	 *
	 * @param allStatements
	 */
	private void generateDeletes(List<String> allStatements)
	{
		for(TableWriteSet tableWriteSet : this.txnWriteSet.values())
			tableWriteSet.generateDeleteStatements(allStatements);
	}

	private void generateInserts(List<String> allStatements)
	{
		for(TableWriteSet tableWriteSet : this.txnWriteSet.values())
			tableWriteSet.generateInsertsStatements(allStatements);
	}

	public List<RequestEntry> verifyInvariants()
	{
		List<RequestEntry> checkEntryList = new ArrayList<>();

		for(TableWriteSet writeSet : this.txnWriteSet.values())
			this.verifyInvariantsForTable(writeSet, checkEntryList);

		return checkEntryList;
	}

	public void processCoordinatorResponse(List<ResponseEntry> results)
	{
		for(ResponseEntry result : results)
		{
			if(result.getResquestedValue() != null)
			{
				String pkValueString = result.getId();
				String fieldName = result.getFieldName();
				String tableName = result.getTableName();
				PrimaryKeyValue pkValue = new PrimaryKeyValue(pkValueString, tableName);

				this.modifiedTuples.get(pkValue).addLwwEntry(fieldName, result.getResquestedValue());
				LOG.trace("tuple updated with value received from coordinator");
			}
		}
	}

	private void verifyInvariantsForTable(TableWriteSet writeSet, List<RequestEntry> checkList)
	{
		for(TupleWriteSet tupleWriteSet : writeSet.getTuplesWriteSet())
			this.verifyInvariantsForTuple(tupleWriteSet, writeSet.getTableName(), tupleWriteSet.getTuplePkValue(),
					checkList);
	}

	private void verifyInvariantsForTuple(TupleWriteSet tupleWriteSet, String tableName, PrimaryKeyValue pkValue,
										  List<RequestEntry> checkList)
	{
		DatabaseTable table = Configuration.getInstance().getDatabaseMetadata().getTable(tableName);
		Map<String, String> lwwFields = tupleWriteSet.getModifiedValuesMap();
		Map<String, String> oldFields = tupleWriteSet.getOldValuesMap();

		for(String fieldName : lwwFields.keySet())
		{
			DataField field = table.getField(fieldName);
			if(!field.hasInvariants())
				continue;

			for(Constraint constraint : field.getInvariants())
			{
				if(this.treatedConstraints.contains(constraint.getConstraintIdentifier()))
					continue;

				this.treatedConstraints.add(constraint.getConstraintIdentifier());
				RequestEntry newEntry = new RequestEntry();
				newEntry.setId(pkValue.getValue());
				newEntry.setTableName(tableName);
				newEntry.setFieldName(fieldName);
				newEntry.setConstraintId(constraint.getConstraintIdentifier());

				switch(constraint.getType())
				{
				case AUTO_INCREMENT:
					newEntry.setType(RequestType.REQUEST_ID);
					this.modifiedTuples.put(pkValue, tupleWriteSet);
					LOG.trace("new request Id check entry added for field {}", fieldName);
					checkList.add(newEntry);
					break;
				case UNIQUE:
					newEntry.setType(RequestType.UNIQUE);

					StringBuilder buffer = new StringBuilder();

					Iterator<DataField> it = constraint.getFields().iterator();
					while(it.hasNext())
					{
						DataField currField = it.next();
						if(lwwFields.containsKey(currField.getFieldName()))
							buffer.append(lwwFields.get(currField.getFieldName()));
						else
							buffer.append(oldFields.get(currField.getFieldName()));
						if(it.hasNext())
							buffer.append(",");
					}
					newEntry.setValue(buffer.toString());
					LOG.trace("new unique check entry added for field {}", fieldName);
					checkList.add(newEntry);
					break;
				case CHECK:
					String newValue = lwwFields.get(fieldName);
					String oldValue = oldFields.get(fieldName);
					if(((CheckConstraint) constraint).mustCoordinate(newValue, oldValue))
					{
						newEntry.setType(RequestType.APPLY_DELTA);
						//FIXME: use delta instead of final result?
						newEntry.setValue(((CheckConstraint) constraint).calculateDelta(newValue, oldValue));
						checkList.add(newEntry);
						LOG.trace("new delta check entry added for field {}", fieldName);
					}
					break;
				case FOREIGN_KEY:
					RuntimeHelper.throwRunTimeException("invariant not supported yet", ExitCode
							.MISSING_IMPLEMENTATION);
					break;
				default:
					LOG.error("unexpected constraint ");
					RuntimeHelper.throwRunTimeException("unexpected constraint", ExitCode.UNEXPECTED_OP);
				}
			}
		}

	}
}
