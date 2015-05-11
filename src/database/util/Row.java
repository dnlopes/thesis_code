package database.util;


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

	public Row(DatabaseTable databaseTable, PrimaryKeyValue pkValue)
	{
		this.table = databaseTable;
		this.pkValue = pkValue;
		this.fieldValues = new HashMap<>();
	}


	public void addFieldValue(FieldValue newValue)
	{
		this.fieldValues.put(newValue.getFieldName(), newValue);
	}

	public FieldValue getFieldValue(String fieldName)
	{
		return this.fieldValues.get(fieldName);
	}
	
}
