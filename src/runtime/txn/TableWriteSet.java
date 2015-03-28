package runtime.txn;


import database.util.PrimaryKeyValue;

import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TableWriteSet
{

	// original name, not temporary
	private String tableName;
	// list of tupleId that were deleted, inserted and updates
	private Set<PrimaryKeyValue> deletedTuples;
	private Set<PrimaryKeyValue> insertedTuples;
	private Set<PrimaryKeyValue> updatedTuples;
	private Map<PrimaryKeyValue, TupleWriteSet> writeSet;

	public TableWriteSet(String tableName)
	{
		this.tableName = tableName;
		this.deletedTuples = new HashSet<>();
		this.insertedTuples = new HashSet<>();
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

	public void addInsertedRow(PrimaryKeyValue id)
	{
		this.insertedTuples.add(id);
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

	public Set<PrimaryKeyValue> getInsertedRows()
	{
		return this.insertedTuples;
	}

	public boolean hasDeletedRows()
	{
		return this.deletedTuples.size() > 0;
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
}
