package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class RequestValue
{
	private String table;
	private String field;
	//this value should be assigned by the coordinator
	private String value;

	public RequestValue (String tableName, String fieldName)
	{
		this.table = tableName;
		this.field = fieldName;
	}
	
}
