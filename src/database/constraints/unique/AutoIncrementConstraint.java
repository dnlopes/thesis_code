package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;


/**
 * Created by dnlopes on 24/03/15.
 */
public class AutoIncrementConstraint extends AbstractConstraint
{

	private boolean isPrimaryKey;
	public AutoIncrementConstraint(boolean isPrimaryKey)
	{
		super(ConstraintType.AUTO_INCREMENT);
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}
}
