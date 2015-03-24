package runtime.txn;


import java.sql.ResultSet;
import java.util.*;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TableWriteSet
{

	// original name, not temporary
	private String tableName;
	// list of tupleId that were deleted
	private Set<Integer> deletedTuples;
	private Set<Integer> insertedTuples;
	private Set<Integer> updatedTuples;
	private Map<Integer, TupleWriteSet> writeSet;

	public TableWriteSet(String tableName)
	{
		this.tableName = tableName;
		this.deletedTuples = new HashSet<>();
		this.insertedTuples = new HashSet<>();
		this.updatedTuples = new HashSet<>();
		this.writeSet = new LinkedHashMap();
	}

	public void addDeletedRow(Integer id)
	{
		this.deletedTuples.add(id);
	}

	public void addInsertedRow(Integer id)
	{
		this.insertedTuples.add(id);
	}

	public void removeUpdatedRow(Integer id)
	{
		this.updatedTuples.remove(id);
	}

	public void addUpdatedRow(Integer id)
	{
		this.updatedTuples.add(id);
	}

	public Set<Integer> getDeletedRows()
	{
		return this.deletedTuples;
	}

	public boolean hasDeletedRows()
	{
		return this.deletedTuples.size() > 0;
	}

	public boolean hasInsertedRows()
	{
		return this.insertedTuples.size() > 0;
	}

	public boolean hasUpdatedRows()
	{
		return this.updatedTuples.size() > 0;
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

	public void setWriteSet(Map<Integer, TupleWriteSet> writeSet)
	{
		this.writeSet = writeSet;
	}

	public Collection<TupleWriteSet> getTuplesWriteSet()
	{
		return this.writeSet.values();
	}
}