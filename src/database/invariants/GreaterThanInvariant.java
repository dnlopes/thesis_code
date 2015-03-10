package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 10/03/15.
 */
public class GreaterThanInvariant extends Invariant
{

	private String minValue;

	public GreaterThanInvariant(DatabaseTable table, DataField field, String threshold)
	{
		super(table, field);
		this.minValue = threshold;
	}

	public String getMinValue()
	{
		return this.minValue;
	}
}
