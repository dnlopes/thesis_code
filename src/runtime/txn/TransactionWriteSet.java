package runtime.txn;


import database.constraints.*;
import database.constraints.check.CheckConstraint;
import database.util.DataField;
import database.util.DatabaseTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.Configuration;
import util.defaults.ScratchpadDefaults;
import util.thrift.CheckTypeRequest;
import util.thrift.ThriftCheckEntry;
import util.thrift.ThriftCheckResult;

import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TransactionWriteSet
{

	private static final Logger LOG = LoggerFactory.getLogger(TransactionWriteSet.class);

	private Map<String, TableWriteSet> txnWriteSet;
	private List<String> statements;

	// list of tuples that will be modified after contacting the coordinator
	// for instance when requesting a new id
	private Map<Integer, TupleWriteSet> modifiedTuples;

	public TransactionWriteSet()
	{
		this.txnWriteSet = new HashMap<>();
		this.statements = new ArrayList<>();
		this.modifiedTuples = new HashMap<>();
	}

	public void resetWriteSet()
	{
		for(TableWriteSet tableSet : this.txnWriteSet.values())
			tableSet.reset();

		this.txnWriteSet.clear();
		this.statements.clear();
		this.modifiedTuples.clear();
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
		//TODO inserts

		this.statements = allStatements;
	}

	public List<String> getStatements()
	{
		return this.statements;
	}

	private void generateUpdates(List<String> allStatements) throws SQLException
	{
		// gerar aqui os comandos sql ou dar o write set inteiro ao replicator?
		for(TupleWriteSet set : this.modifiedTuples.values())
		{
		}
		/*
		StringBuilder buffer = new StringBuilder();

		for(TableWriteSet tableWriteSet : this.writeSet.values())
		{
			if(!tableWriteSet.hasUpdatedRows())
				continue;

			ResultSet rs = tableWriteSet.getUpdateResultSet();

			while(rs.next())
			{
				buffer.append("UPDATE ");
				buffer.append(tableWriteSet.getTableName());
				buffer.append(" SET ");

				//for each modified column
				for(String column : tableWriteSet.getModifiedColumns())
				{
					// ignore custom fields. there is no point in updating it
					if(column.startsWith(ScratchpadDefaults.SCRATCHPAD_COL_PREFIX))
						continue;

					String newValue = rs.getString(column);
					DataField field = DatabaseMetadata.getField(tableWriteSet.getTableName(), column);

					if(field.hasInvariants())
						InvariantChecker.checkInvariants(DBOperationType.UPDATE, field, newValue,
								this.invariantsToCheck);

					buffer.append(column);
					buffer.append("=");
					buffer.append(newValue);
					buffer.append(",");
				}

				buffer.deleteCharAt(buffer.length() - 1);

				buffer.append(" WHERE ");
				buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
				buffer.append("=");
				buffer.append(rs.getString(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE));
				allStatements.add(buffer.toString());
				buffer.setLength(0);
			}
		}
		*/
	}

	/**
	 * Generates all delete statements for a given transaction
	 *
	 * @param allStatements
	 */
	private void generateDeletes(List<String> allStatements)
	{
		StringBuilder buffer = new StringBuilder();

		for(TableWriteSet tableWriteSet : this.txnWriteSet.values())
		{
			if(!tableWriteSet.hasDeletedRows())
				continue;

			// add delete statements
			buffer.append("DELETE from ");
			buffer.append(tableWriteSet.getTableName());
			buffer.append(" WHERE (");
			buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
			buffer.append(" IN ");
			this.addDeletedClause(buffer, tableWriteSet.getDeletedRows());
			buffer.append(")");
			allStatements.add(buffer.toString());
			buffer.setLength(0);
		}
	}

	private void addDeletedClause(StringBuilder buffer, Set<Integer> deletedRows)
	{
		boolean first = true;
		for(Integer tupleId : deletedRows)
		{
			if(first)
			{
				first = false;
				buffer.append("(");
				buffer.append(tupleId);
			} else
			{
				buffer.append(",");
				buffer.append(tupleId);
			}
		}
		buffer.append(")");
	}

	public List<ThriftCheckEntry> verifyInvariants()
	{
		List<ThriftCheckEntry> checkEntryList = new ArrayList<>();

		for(TableWriteSet writeSet : this.txnWriteSet.values())
			this.verifyInvariantsForTable(writeSet, checkEntryList);

		return checkEntryList;
	}

	public void processCoordinatorResponse(List<ThriftCheckResult> results)
	{
		for(ThriftCheckResult result : results)
		{
			if(result.getResquestedValue() != null)
			{
				int rowId = result.getId();
				String fieldName = result.getFieldName();
				this.modifiedTuples.get(rowId).addLwwEntry(fieldName, result.getResquestedValue());
				LOG.trace("tuple updated with value received from coordinator");
			}
		}
	}

	private void verifyInvariantsForTable(TableWriteSet writeSet, List<ThriftCheckEntry> checkList)
	{
		for(TupleWriteSet tupleWriteSet : writeSet.getTuplesWriteSet())
			this.verifyInvariantsForTuple(tupleWriteSet, writeSet.getTableName(), tupleWriteSet.getTuplePkValue(),
					checkList);
	}

	private void verifyInvariantsForTuple(TupleWriteSet tupleWriteSet, String tableName, int rowId,
										  List<ThriftCheckEntry> checkList)
	{
		DatabaseTable table = Configuration.getInstance().getDatabaseMetadata().getTable(tableName);
		Map<String, String> newModifiedFields = tupleWriteSet.getModifiedValuesMap();
		Map<String, String> oldFields = tupleWriteSet.getOldValuesMap();

		for(String fieldName : newModifiedFields.keySet())
		{
			DataField field = table.getField(fieldName);
			if(!field.hasInvariants())
				continue;

			ThriftCheckEntry newEntry = new ThriftCheckEntry();
			newEntry.setId(rowId);
			newEntry.setTableName(tableName);
			newEntry.setFieldName(fieldName);

			for(Constraint constraint : field.getInvariants())
			{
				switch(constraint.getType())
				{
				case UNIQUE:
					if(!field.isAutoIncrement())
					{
						newEntry.setType(CheckTypeRequest.UNIQUE);
						newEntry.setValue(newModifiedFields.get(fieldName));
						LOG.trace("new unique check entry added for field {}", fieldName);
					} else
					{
						newEntry.setType(CheckTypeRequest.REQUEST_ID);
						this.modifiedTuples.put(rowId, tupleWriteSet);
						LOG.trace("new request Id check entry added for field {}", fieldName);
					}
					checkList.add(newEntry);
					break;
				case CHECK:
					String newValue = newModifiedFields.get(fieldName);
					String oldValue = oldFields.get(fieldName);
					if(((CheckConstraint) constraint).mustCoordinate(newValue, oldValue))
					{
						newEntry.setType(CheckTypeRequest.APPLY_DELTA);
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
