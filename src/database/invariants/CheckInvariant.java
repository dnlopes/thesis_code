package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public abstract class CheckInvariant
{

	protected int rowId;

	public CheckInvariant(int rowId)
	{
		this.rowId = rowId;
	}
}
