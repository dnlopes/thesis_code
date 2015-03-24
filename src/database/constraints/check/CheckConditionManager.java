package database.constraints.check;


import database.util.DataField;


/**
 * Created by dnlopes on 24/03/15.
 */
public class CheckConditionManager<T>
{

	private CheckConstraint condition;
	private DataField field;
	private T currentValue;

	public CheckConditionManager(DataField field, CheckConstraint constraint)
	{
		this.condition = constraint;
		this.field = field;
	}

	public boolean violates(T value)
	{
		return true;
	}

	
}
