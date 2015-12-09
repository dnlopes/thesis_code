package client.execution.temporary;


import common.database.Record;
import common.database.field.DataField;
import common.database.table.DatabaseTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 06/12/15.
 */
public class WriteSet
{

	private Map<String, Record> inserts;
	private Map<String, Record> updates;
	private Map<String, Record> deletes;
	private Map<String, Record> cachedRecords;
	private Map<String, Record> deletedRecords;

	public WriteSet()
	{
		this.inserts = new HashMap<>();
		this.updates = new HashMap<>();
		this.deletes = new HashMap<>();
		this.cachedRecords = new HashMap<>();
		this.deletedRecords = new HashMap<>();
	}

	public void addToCache(Record record)
	{
		String pkValue = record.getPkValue().getUniqueValue();
		cachedRecords.put(pkValue, record);
	}

	public Map<String, Record> getCachedRecords()
	{
		return cachedRecords;
	}

	public Map<String, Record> getDeletedRecords()
	{
		return deletedRecords;
	}

	public void addToInserts(Record record) throws SQLException
	{
		String pkValue = record.getPkValue().getUniqueValue();
		if(inserts.containsKey(pkValue))
			throw new SQLException("duplicated record entry");

		this.inserts.put(pkValue, record);
	}

	public void addToUpdates(Record record)
	{
		String pkValue = record.getPkValue().getUniqueValue();

		if(updates.containsKey(pkValue))
		{
			DatabaseTable table = record.getDatabaseTable();

			// record was updated twice, lets merge the values
			Record oldRecord = updates.get(pkValue);

			for(Map.Entry<String, String> dataEntry : record.getRecordData().entrySet())
			{
				DataField field = table.getField(dataEntry.getKey());

				if(field.isPrimaryKey())
					continue;
				if(field.isLWWField())
					oldRecord.addData(dataEntry.getKey(), dataEntry.getValue());
				else if(field.isDeltaField())
				{
					//TODO
					// calculate delta and merge
				}
			}
		} else
			this.updates.put(pkValue, record);
	}

	public void addToDeletes(Record record)
	{
		String pkValue = record.getPkValue().getUniqueValue();
		this.deletedRecords.put(pkValue, record);

		if(cachedRecords.containsKey(pkValue))
			cachedRecords.remove(pkValue);
	}

	public Map<String, Record> getInserts()
	{
		return inserts;
	}

	public Map<String, Record> getUpdates()
	{
		return updates;
	}

	public Map<String, Record> getDeletes()
	{
		return deletes;
	}

	public void clear()
	{
		this.inserts.clear();
		this.updates.clear();
		this.deletes.clear();
		this.cachedRecords.clear();
		this.deletedRecords.clear();
	}
	
}
