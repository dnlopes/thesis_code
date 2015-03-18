package database.invariants;

import database.util.DataField;


/**
 * Created by dnlopes on 17/03/15.
 * This class is a pair <Datafield, Value>.
 * It is used to send to the coordinaton service so he can validate this pair.
 * For instance, if the field is a unique field, the value represents the "desired" value.
 * The coordinator service will then verify if this value is already used and respond accordingly.
 */
public class FieldValuePair
{

	private final DataField field;
	private final String value;
	private boolean isValid;

	public FieldValuePair(DataField field, String value)
	{
		this.field = field;
		this.value = value;
		this.isValid = false;
	}

	public void validateInvariant()
	{
		this.isValid = true;
	}

}
