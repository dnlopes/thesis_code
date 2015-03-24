package database.invariants;


/**
 * Created by dnlopes on 17/03/15.
 * This class is a pair <Datafield, Value>.
 * It is used to send to the coordinaton service so he can validate this pair.
 * For instance, if the field is a unique field, the value represents the "desired" value.
 * The coordinator service will then verify if this value is already used and respond accordingly.
 */
public class UniqueValue extends CheckInvariantItem
{

	private String value;

	public UniqueValue(int rowId, int id, String tableName, String fieldName, String uniqueValue)
	{
		super(InvariantType.UNIQUE, rowId, id, fieldName, tableName);
		this.value = uniqueValue;
	}
}
