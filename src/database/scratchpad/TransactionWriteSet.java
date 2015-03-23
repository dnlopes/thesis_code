package database.scratchpad;


import database.invariants.InvariantChecker;
import database.invariants.Value;
import database.jdbc.util.DBOperationType;
import database.util.DataField;
import database.util.DatabaseMetadata;
import util.defaults.ScratchpadDefaults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TransactionWriteSet
{

	private Map<String, TableWriteSet> writeSet;
	private List<String> statements;
	private List<Value> invariantsToCheck;

	public TransactionWriteSet()
	{
		this.writeSet = new HashMap<>();
		this.statements = new ArrayList<>();
		this.invariantsToCheck = new ArrayList<>();
	}

	public void addTableWriteSet(String tableName, TableWriteSet writeSet)
	{
		this.writeSet.put(tableName, writeSet);
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
						InvariantChecker.checkInvariants(DBOperationType.UPDATE, field, newValue, this.invariantsToCheck);

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
	}

	/**
	 * Generates all delete statements for a given transaction
	 *
	 * @param allStatements
	 */
	private void generateDeletes(List<String> allStatements)
	{
		StringBuilder buffer = new StringBuilder();

		for(TableWriteSet tableWriteSet : this.writeSet.values())
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
}
