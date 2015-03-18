package database.invariants;


/**
 * Created by dnlopes on 17/03/15.
 * This class is a pair <Datafield, Value>.
 * It is used to send to the coordinaton service so he can validate this pair.
 * For instance, if the field is a unique field, the value represents the "desired" value.
 * The coordinator service will then verify if this value is already used and respond accordingly.
 */
public class CheckValue
{

	private String field;
	private String value;
	private String table;
	private boolean isValid;

	public CheckValue(String tableName, String fieldName, String value)
	{
		this.field = fieldName;
		this.table = tableName;
		this.value = value;
		this.isValid = false;
	}
}
