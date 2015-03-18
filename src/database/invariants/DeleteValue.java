package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class DeleteValue extends Value
{

	private String field;
	private String value;
	private String table;

	public DeleteValue(String tableName, String fieldName, String value)
	{
		this.field = fieldName;
		this.table = tableName;
		this.value = value;
	}
	
}
