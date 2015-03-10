package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public abstract class Invariant
{

	private DatabaseTable table;
	private DataField field;


	protected Invariant(DatabaseTable table, DataField field)
	{
		this.table = table;
		this.field = field;
	}

	public DatabaseTable getTable()
	{
		return this.table;
	}


	public DataField getField()
	{
		return this.field;
	}
}
