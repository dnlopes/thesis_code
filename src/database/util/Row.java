package database.util;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 11/05/15.
 */
public class Row
{

	private DatabaseTable table;


	private PrimaryKeyValue pkValue;
	private Map<String, FieldValue> fieldValues;
	private Map<String, FieldValue> newFieldValues;

	private boolean hasSideEffects;

	public Row(DatabaseTable databaseTable, PrimaryKeyValue pkValue)
	{
		this.table = databaseTable;
		this.pkValue = pkValue;
		this.fieldValues = new HashMap<>();
		this.newFieldValues = new HashMap<>();
		this.hasSideEffects = false;
	}

	public void updateFieldValue(FieldValue newValue)
	{
		this.newFieldValues.put(newValue.getDataField().getFieldName(), newValue);

		if(newValue.getDataField().hasChilds())
			this.hasSideEffects = true;
	}

	public void addFieldValue(FieldValue value)
	{
		this.fieldValues.put(value.getDataField().getFieldName(), value);
	}

	public FieldValue getFieldValue(String fieldName)
	{
		return this.fieldValues.get(fieldName);
	}

	public Collection<FieldValue> getUpdatedFields()
	{
		return this.newFieldValues.values();
	}

	public boolean hasSideEffects()
	{
		return this.hasSideEffects;
	}

	public Collection<FieldValue> getFieldValues()
	{
		return this.fieldValues.values();
	}

	public DatabaseTable getTable()
	{
		return this.table;
	}

	public PrimaryKeyValue getPkValue()
	{
		return this.pkValue;
	}
}
