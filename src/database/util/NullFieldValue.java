package database.util;


import database.util.field.DataField;


/**
 * Created by dnlopes on 23/06/15.
 */
public class NullFieldValue extends FieldValue
{
	
	public NullFieldValue(DataField field, String value)
	{
		super(field, value);
	}

	@Override
	public String getFormattedValue()
	{
		return value;
	}
}
