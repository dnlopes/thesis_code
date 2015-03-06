package invariants;

import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public interface Invariant
{
	public DatabaseTable getTable();
}
