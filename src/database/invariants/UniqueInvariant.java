package database.invariants;

import database.util.DataField;


/**
 * Created by dnlopes on 06/03/15.
 */
public class UniqueInvariant extends Invariant
{

	public UniqueInvariant(DataField field, String declaration)
	{
		super(field, declaration);
	}
}
