package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public class LesserThanInvariant implements Invariant
{

	private String left, right;

	public LesserThanInvariant(String left, String right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	public DatabaseTable getTable()
	{
		return null;
	}
}
