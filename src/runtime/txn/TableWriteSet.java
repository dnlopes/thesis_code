package runtime.txn;


import database.util.DatabaseTable;
import database.util.PrimaryKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;

import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TableWriteSet
{

	private static final Logger LOG = LoggerFactory.getLogger(TableWriteSet.class);

	// original name, not temporary
	private String tableName;
	// list of tupleId that were deleted, inserted and updates
	private Set<PrimaryKeyValue> deletedTuples;
	private Map<PrimaryKeyValue, TupleWriteSet> insertedTuples;
	private Map<PrimaryKeyValue, TupleWriteSet> updatedTuples;

	public TableWriteSet(String tableName)
	{
		this.tableName = tableName;
		this.deletedTuples = new HashSet<>();
		this.insertedTuples = new HashMap<>();
		this.updatedTuples = new HashMap<>();
	}

	public void addUpdatedRow(PrimaryKeyValue pkValue, TupleWriteSet writeSet)
	{
		this.updatedTuples.put(pkValue, writeSet);
	}

	public TupleWriteSet getTupleWriteSet(PrimaryKeyValue pkValue)
	{
		if(this.insertedTuples.containsKey(pkValue))
			return this.insertedTuples.get(pkValue);
		else if(this.updatedTuples.containsKey(pkValue))
			return this.updatedTuples.get(pkValue);
		else
			return null;
	}

	public Collection<TupleWriteSet> getInsertedTuplesWriteSet()
	{
		return this.insertedTuples.values();
	}

	public Collection<TupleWriteSet> getUpdatedTuplesWriteSet()
	{
		return this.updatedTuples.values();
	}

	public void addDeletedRow(PrimaryKeyValue id)
	{
		this.deletedTuples.add(id);
	}

	public void addInsertedRow(PrimaryKeyValue pkValue, TupleWriteSet writeSet)
	{
		this.insertedTuples.put(pkValue, writeSet);
	}

	public void removeUpdatedRow(PrimaryKeyValue id)
	{
		this.updatedTuples.remove(id);
	}

	public void removeInsertedRow(PrimaryKeyValue id)
	{
		this.insertedTuples.remove(id);
	}

	public Set<PrimaryKeyValue> getDeletedRows()
	{
		return this.deletedTuples;
	}

	public Map<PrimaryKeyValue, TupleWriteSet> getInsertedRows()
	{
		return this.insertedTuples;
	}

	public void reset()
	{
		this.deletedTuples.clear();
		this.insertedTuples.clear();
		this.updatedTuples.clear();
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public void generateDeleteStatements(List<String> statements)
	{
		if(this.deletedTuples.size() == 0)
			return;

		LOG.debug("{} tuples deleted", this.deletedTuples.size());
		StringBuilder buffer = new StringBuilder();
		DatabaseTable table = Configuration.getInstance().getDatabaseMetadata().getTable(this.tableName);
		String pkCols = table.getPrimaryKey().getQueryClause();

		// add delete statements
		buffer.append("DELETE from ");
		buffer.append(this.tableName);
		buffer.append(" WHERE (");
		buffer.append(pkCols);
		buffer.append(") IN (");
		buffer.append(this.createInValuesClause());
		buffer.append(")");

		String statement = buffer.toString();
		LOG.debug("statement generated: {}", statement);
		statements.add(statement);
	}

	public void generateUpdateStatements(List<String> statements)
	{
		if(this.updatedTuples.size() == 0)
			return;

		LOG.debug("{} tuples updated", this.updatedTuples.size());
		for(PrimaryKeyValue pkValue : this.updatedTuples.keySet())
		{
			TupleWriteSet tupleWriteSet = this.updatedTuples.get(pkValue);
			tupleWriteSet.generateUpdateStatement(statements);
		}
	}

	public void generateInsertsStatements(List<String> statements)
	{
		if(this.insertedTuples.size() == 0)
			return;

		LOG.debug("{} tuples inserted", this.insertedTuples.size());
		for(PrimaryKeyValue pkValue : this.insertedTuples.keySet())
		{
			TupleWriteSet tupleWriteSet = this.insertedTuples.get(pkValue);
			tupleWriteSet.generateInsertStatement(statements);
		}
	}

	private String createInValuesClause()
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<PrimaryKeyValue> it = this.getDeletedRows().iterator();

		while(it.hasNext())
		{
			PrimaryKeyValue pkValue = it.next();

			buffer.append("(");
			buffer.append(pkValue.getValue());
			buffer.append(")");

			if(it.hasNext())
				buffer.append(", ");
		}
		return buffer.toString();
	}
}
