package database.scratchpad;


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
	private Set<String> deletedTuples;
	private Set<String> insertedTuples;
	private Set<String> updatedTuples;
	private Set<String> modifiedColumns;

	private ResultSet updateResultSet;
	private ResultSet insertResultSet;

	public TableWriteSet(String tableName)
	{
		this.tableName = tableName;
		this.deletedTuples = new HashSet<>();
		this.insertedTuples = new HashSet<>();
		this.updatedTuples = new HashSet<>();
		this.modifiedColumns = new HashSet<>();
	}

	public void addDeletedRow(String id)
	{
		this.deletedTuples.add(id);
	}

	public void addInsertedRow(String id)
	{
		this.insertedTuples.add(id);
	}

	public void removeUpdatedRow(String id)
	{
		this.updatedTuples.remove(id);
	}

	public void addUpdatedRow(String id)
	{
		this.updatedTuples.add(id);
	}

	public void addModifiedColumns(String column)
	{
		this.modifiedColumns.add(column);
	}

	public Set<String> getModifiedColumns()
	{
		return this.modifiedColumns;
	}

	public Set<String> getDeletedRows()
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
		this.modifiedColumns.clear();
		this.updatedTuples.clear();
		this.updateResultSet = null;
		this.insertResultSet = null;
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public void setUpdateResultSet(ResultSet rs)
	{
		this.updateResultSet = rs;
	}

	public ResultSet getUpdateResultSet()
	{
		return this.updateResultSet;
	}

	public void setInsertResultSet(ResultSet rs)
	{
		this.insertResultSet = rs;
	}

	public ResultSet getInsertResultSet()
	{
		return this.insertResultSet;
	}

}
