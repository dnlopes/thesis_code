package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public class DeleteValue extends CheckInvariantItem
{

	private String value;

	public DeleteValue(int rowId, int id, String tableName, String fieldName, String value)
	{
		super(InvariantType.DELETE_VALUE, rowId, id, fieldName, tableName);
		this.value = value;
	}
	
}
