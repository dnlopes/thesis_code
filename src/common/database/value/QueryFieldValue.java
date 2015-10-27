package common.database.value;


import common.database.field.DataField;


/**
 * Created by dnlopes on 17/05/15.
 */
public class QueryFieldValue extends FieldValue
{
	
	public QueryFieldValue(DataField field, String value)
	{
		super(field, value);
	}

	@Override
	public String getFormattedValue()
	{
		return "(" + this.value + ")";
	}
}
