package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class DeleteValue extends CheckInvariant
{

	private String field;
	private String value;
	private String table;

	public DeleteValue(int rowId, String tableName, String fieldName, String value)
	{
		super(rowId);
		this.field = fieldName;
		this.table = tableName;
		this.value = value;
	}
	
}
