package database.invariants;

import database.util.DataField;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ForeignKeyInvariant extends Invariant
{
	private String referenceField;
	private String referenceTable;

	public ForeignKeyInvariant(DataField field, String referenceTable, String referenceField, String declaration)
	{
		super(field, declaration);
		this.referenceField = referenceField;
		this.referenceTable = referenceTable;
	}

	public String getReferenceField()
	{
		return this.referenceField;
	}

	public String getReferenceTable()
	{
		return this.referenceTable;
	}
}
