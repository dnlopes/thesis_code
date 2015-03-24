package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class RequestValue extends CheckInvariant
{
	private String table;
	private String field;
	//this value should be assigned by the coordinator
	private String value;

	public RequestValue (int rowId, String tableName, String fieldName)
	{
		super(rowId);
		this.table = tableName;
		this.field = fieldName;
	}
	
}
