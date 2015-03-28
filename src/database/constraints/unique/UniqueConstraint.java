package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;


/**
 * Created by dnlopes on 24/03/15.
 */
public class UniqueConstraint extends AbstractConstraint
{

	private boolean isPrimaryKey;
	public UniqueConstraint(boolean isPrimaryKey)
	{
		super(ConstraintType.UNIQUE);
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}
}
