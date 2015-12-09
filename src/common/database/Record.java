package common.database;


import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.util.PrimaryKeyValue;
import common.database.value.FieldValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 05/12/15.
 */
public class Record
{

	private Map<String, DataField> pkFields;
	private PrimaryKeyValue pkValue;
	private Map<String, String> data;
	private DatabaseTable databaseTable;
	private Map<String, DataField> normalFields;
	private Map<String, String> symbolsToFieldMapping;
	private Map<String, String> fieldToSymbolsMapping;

	private boolean touchedLWWField;

	public Record(DatabaseTable table)
	{
		this.databaseTable = table;
		this.pkFields = this.databaseTable.getPrimaryKey().getPrimaryKeyFields();
		this.normalFields = this.databaseTable.getNormalFields();
		this.symbolsToFieldMapping = new HashMap<>();
		this.fieldToSymbolsMapping = new HashMap<>();

		this.data = new HashMap<>();

		this.pkValue = new PrimaryKeyValue(this.databaseTable);
		this.touchedLWWField = false;
	}

	public Record(DatabaseTable table, Map<String, String> data, Map<String, String> symbolToField,
				  Map<String, String> fieldToSymbol, PrimaryKeyValue pkValue, boolean touchedLWWField)
	{
		this.databaseTable = table;
		this.pkFields = this.databaseTable.getPrimaryKey().getPrimaryKeyFields();
		this.normalFields = this.databaseTable.getNormalFields();

		this.data = data;
		this.pkValue = pkValue;
		this.touchedLWWField = touchedLWWField;
		this.symbolsToFieldMapping = symbolToField;
		this.fieldToSymbolsMapping = fieldToSymbol;
	}

	public PrimaryKeyValue getPkValue()
	{
		return pkValue;
	}

	public void addData(String key, String value)
	{
		if(pkFields.containsKey(key) && !data.containsKey(key))
		{
			FieldValue fValue = new FieldValue(databaseTable.getField(key), value);
			pkValue.addFieldValue(fValue);
		}

		data.put(key, value);

		if(normalFields.containsKey(key) && normalFields.get(key).isLWWField())
			touchedLWWField = true;
	}

	public String getData(String key)
	{
		return this.data.get(key);
	}

	public void mergeRecords(Record oldRecord)
	{
		for(DataField aField : this.databaseTable.getNormalFields().values())
		{
			String fieldName = aField.getFieldName();

			if(aField.isDeltaField())
			{
				double oldValue = Double.parseDouble(oldRecord.getData(fieldName));
				double newValue = Double.parseDouble(data.get(fieldName));

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
				this.data.put(fieldName, applyDelta);
			} else
			{
				if(this.data.containsKey(fieldName)) // this field was updated
				{
					if(aField.isLWWField())
						this.touchedLWWField = true;

				} else // this field was not updated
					this.data.put(fieldName, oldRecord.getData(fieldName));
			}
		}
	}

	public boolean isPrimaryKeyReady()
	{
		return pkValue.isPrimaryKeyReady();
	}

	public boolean containsEntry(String key)
	{
		return this.data.containsKey(key);
	}

	public Map<String, String> getRecordData()
	{
		return data;
	}

	public void setPkValue(PrimaryKeyValue pkValue)
	{
		this.pkValue = pkValue;
	}

	public boolean touchedLWWField()
	{
		return touchedLWWField;
	}

	public boolean isFullyCached()
	{
		return data.size() == normalFields.size();
	}

	public void addSymbolEntry(String symbol, String fieldName)
	{
		symbolsToFieldMapping.put(symbol, fieldName);
		fieldToSymbolsMapping.put(fieldName, symbol);
	}

	public Collection<String> getAllUsedSymbols()
	{
		return symbolsToFieldMapping.keySet();
	}

	public boolean containsSymbolForField(String fieldName)
	{
		return fieldToSymbolsMapping.containsKey(fieldName);
	}

	public String getSymbolForField(String fieldName)
	{
		return fieldToSymbolsMapping.get(fieldName);
	}

	public DatabaseTable getDatabaseTable()
	{
		return databaseTable;
	}

	public Record duplicate()
	{
		return new Record(databaseTable, new HashMap<>(this.data), new HashMap<>(symbolsToFieldMapping),
				new HashMap<>(fieldToSymbolsMapping), pkValue.duplicate(), touchedLWWField);
	}
}
