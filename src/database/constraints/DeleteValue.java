package database.constraints;


/**
 * Created by dnlopes on 18/03/15.
 */
public class DeleteValue extends CheckInvariantItem
{

	private String value;

	public DeleteValue(int rowId, int id, String tableName, String fieldName, String value)
	{
		super(ConstraintType.DELETE_VALUE, rowId, id, fieldName, tableName);
		this.value = value;
	}
	
}
