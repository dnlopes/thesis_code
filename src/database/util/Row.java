package database.util;

import database.constraints.Constraint;

import java.util.*;


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
	private Set<Constraint> contraintsToCheck;

	public Row(DatabaseTable databaseTable, PrimaryKeyValue pkValue)
	{
		this.table = databaseTable;
		this.pkValue = pkValue;
		this.fieldValues = new HashMap<>();
		this.newFieldValues = new HashMap<>();
		this.contraintsToCheck = new HashSet<>();
		this.hasSideEffects = false;
	}

	public void updateFieldValue(FieldValue newValue)
	{

		this.newFieldValues.put(newValue.getDataField().getFieldName(), newValue);

		if(newValue.getDataField().hasChilds())
			this.hasSideEffects = true;

		if(newValue.getDataField().isDeltaField())
			newValue.transformValueForDeltaField(this.fieldValues.get(newValue.getDataField().getFieldName()));
	}

	public void addFieldValue(FieldValue value)
	{
		this.fieldValues.put(value.getDataField().getFieldName(), value);
	}

	public FieldValue getFieldValue(String fieldName)
	{
		return this.fieldValues.get(fieldName);
	}

	public FieldValue getUpdateFieldValue(String fieldName)
	{
		return this.newFieldValues.get(fieldName);
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

	public PrimaryKeyValue getPrimaryKeyValue()
	{
		return this.pkValue;
	}

	public boolean containsNewField(String key)
	{
		return this.newFieldValues.containsKey(key);
	}

	public void mergeUpdates()
	{
		for(Map.Entry<String, FieldValue> entry : this.newFieldValues.entrySet())
		{
			if(entry.getValue().getDataField().isDeltaField())
			{
				FieldValue oldFieldValue = this.fieldValues.get(entry.getKey());

				double oldValue = Double.parseDouble(oldFieldValue.getFormattedValue());
				double newValue = Double.parseDouble(entry.getValue().getFormattedValue());

				double delta = oldValue + newValue;
				String updatedWithDelta = entry.getValue().getDataField().getFieldName() + "+" + String.valueOf(delta);
				entry.getValue().setValue(updatedWithDelta);
			}

			this.fieldValues.put(entry.getKey(), entry.getValue());
		}
	}
}
