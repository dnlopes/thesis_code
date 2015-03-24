package database.constraints;


/**
 * Created by dnlopes on 24/03/15.
 */
public class DeltaValue extends CheckInvariantItem
{

	private String delta;

	public DeltaValue(int rowId, int id, String tableName, String fieldName, String delta)
	{
		super(ConstraintType.DELTA, rowId, id, fieldName, tableName);
		this.delta = delta;
	}

}
