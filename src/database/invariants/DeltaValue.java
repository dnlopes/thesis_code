package database.invariants;


/**
 * Created by dnlopes on 24/03/15.
 */
public class DeltaValue extends CheckInvariant
{

	private String field;
	private String delta;
	private String table;

	public DeltaValue(int rowId, String tableName, String fieldName, String delta)
	{
		super(rowId);
		this.field = fieldName;
		this.table = tableName;
		this.delta = delta;
	}

}
