package database.invariants;

import database.util.DataField;


/**
 * Created by dnlopes on 17/03/15.
 */
public class FieldValuePair
{

	private final DataField field;
	private final String value;

	public FieldValuePair(DataField field, String value)
	{
		this.field = field;
		this.value = value;
	}

}
