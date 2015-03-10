package database.invariants;

import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ForeignKeyInvariant extends Invariant
{
	private DataField referenceField;
	private DatabaseTable referenceTable;

	protected ForeignKeyInvariant(DatabaseTable table, DataField field, DatabaseTable referenceTable, DataField referenceField)
	{
		super(table, field);
		this.referenceField = referenceField;
		this.referenceTable = referenceTable;
	}
}
