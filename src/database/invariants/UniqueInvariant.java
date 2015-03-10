package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public class UniqueInvariant extends Invariant
{

	public UniqueInvariant(DatabaseTable table, DataField field)
	{
		super(table, field);
	}
}
