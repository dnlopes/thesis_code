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
	private Map<PrimaryKeyValue, String> insertedTuples;
	private Set<PrimaryKeyValue> updatedTuples;

	// only for updated tuples
	private Map<PrimaryKeyValue, TupleWriteSet> writeSet;
	private DatabaseTable dbTable;

	public TableWriteSet(String tableName)
	{
		this.tableName = tableName;
		this.dbTable = Configuration.getInstance().getDatabaseMetadata().getTable(this.tableName);
		this.deletedTuples = new HashSet<>();
		this.insertedTuples = new HashMap<>();
		this.updatedTuples = new HashSet<>();
		this.writeSet = new LinkedHashMap();
	}

	public Map<PrimaryKeyValue, TupleWriteSet> getTableWriteSetMap()
	{
		return this.writeSet;
	}

	public void addUpdatedRow(PrimaryKeyValue id)
	{
		this.updatedTuples.add(id);
	}

	public void addDeletedRow(PrimaryKeyValue id)
	{
		this.deletedTuples.add(id);
	}

	public void addInsertedRow(PrimaryKeyValue id, String insertStatement)
	{
		this.insertedTuples.put(id, insertStatement);
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

	public Map<PrimaryKeyValue, String> getInsertedRows()
	{
		return this.insertedTuples;
	}

	public void reset()
	{
		this.deletedTuples.clear();
		this.insertedTuples.clear();
		this.updatedTuples.clear();
		this.writeSet.clear();
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public Collection<TupleWriteSet> getTuplesWriteSet()
	{
		return this.writeSet.values();
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
		buffer.append(" WHERE ( (");
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

		for(PrimaryKeyValue pkValue : this.updatedTuples)
		{
			TupleWriteSet tupleWriteSet = this.writeSet.get(pkValue);
			tupleWriteSet.generateUpdateStatement(statements);
		}
	}

	public void generateInsertsStatements(List<String> statements)
	{
		if(this.insertedTuples.size() == 0)
			return;

		LOG.debug("{} tuples inserted");
		for(PrimaryKeyValue pkValue : this.insertedTuples.keySet())
		{
			String insertStatement = this.insertedTuples.get(pkValue);
			LOG.debug("statement generated: {}", insertStatement);
			statements.add(insertStatement);
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

	public DatabaseTable getDatabaseTable()
	{
		return this.dbTable;
	}
}
