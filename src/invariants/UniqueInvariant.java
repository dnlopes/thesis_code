package invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;

import java.util.List;


/**
 * Created by dnlopes on 06/03/15.
 */
public class UniqueInvariant implements Invariant
{
	private DatabaseTable table;
	private List<DataField> attributes;

	public UniqueInvariant()

	@Override
	public DatabaseTable getTable()
	{
		return null;
	}
}
