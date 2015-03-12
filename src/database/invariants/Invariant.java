package database.invariants;

import database.util.DataField;
import database.util.DatabaseTable;


/**
 * Created by dnlopes on 06/03/15.
 */
public abstract class Invariant
{

	private DataField field;
	private String originalDeclaration;


	protected Invariant(DataField field, String declaration)
	{
		this.field = field;
		this.originalDeclaration = declaration;
	}

	public DataField getField()
	{
		return this.field;
	}


	public String getOriginalDeclaration()
	{
		return this.originalDeclaration;
	}
}
