package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;


/**
 * Created by dnlopes on 24/03/15.
 */
public class UniqueConstraint extends AbstractConstraint
{
	
	public UniqueConstraint()
	{
		super(ConstraintType.UNIQUE);
	}
}