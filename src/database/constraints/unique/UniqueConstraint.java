package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;


/**
 * Created by dnlopes on 24/03/15.
 */
public class UniqueConstraint extends AbstractConstraint
{

	private final boolean isPrimaryKey;
	private final boolean isAutoIncrement;

	public UniqueConstraint(boolean isPrimaryKey, boolean requiresCoordination)
	{
		super(ConstraintType.UNIQUE, requiresCoordination);

		this.isPrimaryKey = isPrimaryKey;
		this.isAutoIncrement = false;
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}

	public boolean isAutoIncrement()
	{
		return this.isAutoIncrement;
	}
}
