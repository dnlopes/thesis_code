package runtime.txn;


import database.util.DatabaseTable;
import database.util.PrimaryKeyValue;


/**
 * Created by dnlopes on 11/05/15.
 */
public class ChildRowWriteSet extends RowWriteSet
{
	public ChildRowWriteSet(PrimaryKeyValue pkValue, DatabaseTable table)
	{
		super(pkValue, table);
	}
}
