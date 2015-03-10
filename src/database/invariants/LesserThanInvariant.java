package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public class LesserThanInvariant extends Invariant
{

	private String threshold;

	public LesserThanInvariant(DatabaseTable table, DataField field, String threshold)
	{
		super(table, field);
		this.threshold = threshold;
	}

	public String getThreshold()
	{
		return this.threshold;
	}
}
