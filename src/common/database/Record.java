package common.database;


import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.util.PrimaryKeyValue;
import common.database.value.FieldValue;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 05/12/15.
 */
public class Record
{

	private Map<String, DataField> pkFields;
	private PrimaryKeyValue pkValue;
	private Map<String, String> recordData;
	private DatabaseTable databaseTable;
	private Map<String, String> attachedSymbols;
	private Map<String, DataField> normalFields;

	private boolean touchedLWWField;

	public Record(DatabaseTable table)
	{
		this.databaseTable = table;
		this.pkFields = this.databaseTable.getPrimaryKey().getPrimaryKeyFields();
		this.normalFields = this.databaseTable.getNormalFields();
		this.recordData = new HashMap<>();
		this.attachedSymbols = new HashMap<>();
		this.pkValue = new PrimaryKeyValue(this.databaseTable.getName());
		this.touchedLWWField = false;
	}

	public PrimaryKeyValue getPkValue()
	{
		return pkValue;
	}

	public void addData(String key, String value)
	{
		this.recordData.put(key, value);

		if(this.normalFields.containsKey(key))
			if(this.normalFields.get(key).isLWWField())
				touchedLWWField = true;

		if(pkFields.containsKey(key))
		{
			FieldValue fValue = new FieldValue(databaseTable.getField(key), value);
			pkValue.addFieldValue(fValue);
		}
	}

	public String getData(String key)
	{
		return this.recordData.get(key);
	}

	public void mergeRecords(Record oldRecord)
	{
		for(DataField aField : this.databaseTable.getNormalFields().values())
		{
			String fieldName = aField.getFieldName();

			if(aField.isDeltaField())
			{
				double oldValue = Double.parseDouble(oldRecord.getData(fieldName));
				double newValue = Double.parseDouble(recordData.get(fieldName));

				double delta = newValue - oldValue;
				String applyDelta;

				if(delta >= 0)
				{
					StringBuilder buffer = new StringBuilder(fieldName);
					buffer.append("+").append(String.valueOf(delta));
					applyDelta = buffer.toString();
				} else
				{
					StringBuilder buffer = new StringBuilder(fieldName);
					buffer.append(String.valueOf(delta));
					applyDelta = buffer.toString();
				}
				this.recordData.put(fieldName, applyDelta);
			} else
			{
				if(this.recordData.containsKey(fieldName)) // this field was updated
				{

					if(aField.isLWWField())
						this.touchedLWWField = true;

				} else // this field was not updated
					this.recordData.put(fieldName, oldRecord.getData(fieldName));
			}
		}
	}

	public boolean containsEntry(String key)
	{
		return this.recordData.containsKey(key);
	}

	public Map<String, String> getRecordData()
	{
		return recordData;
	}

	public void attachSymbol(String field, String symbol)
	{
		this.attachedSymbols.put(field, symbol);
		this.recordData.put(field, symbol);
	}

	public void setPkValue(PrimaryKeyValue pkValue)
	{
		this.pkValue = pkValue;
	}

	public void setRecordData(Map<String, String> recordData)
	{
		this.recordData = recordData;
	}

	public void setAttachedSymbols(Map<String, String> attachedSymbols)
	{
		this.attachedSymbols = attachedSymbols;
	}

	public boolean touchedLWWField()
	{
		return touchedLWWField;
	}

	public Record duplicate()
	{
		Record newRecord = new Record(this.databaseTable);
		newRecord.setRecordData(new HashMap<>(this.recordData));
		newRecord.setAttachedSymbols(new HashMap<>(this.attachedSymbols));

		return newRecord;
	}
}
