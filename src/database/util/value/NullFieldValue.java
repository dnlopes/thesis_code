package database.util.value;


import database.util.field.DataField;


/**
 * Created by dnlopes on 23/06/15.
 */
public class NullFieldValue extends FieldValue
{

	private static final String NULL_VALUE = "NULL";

	public NullFieldValue(DataField field)
	{
		super(field, NULL_VALUE);
	}

	@Override
	public String getFormattedValue()
	{
		return value;
	}
}
