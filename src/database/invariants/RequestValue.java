package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class RequestValue extends CheckInvariantItem
{

	//this value should be assigned by the coordinator
	private String value;

	public RequestValue (int rowId, int id, String tableName, String fieldName)
	{
		super(InvariantType.REQUEST_VALUE, rowId, id, fieldName, tableName);
	}
	
}
