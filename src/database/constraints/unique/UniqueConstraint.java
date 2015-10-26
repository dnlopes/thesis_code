package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import database.util.field.DataField;


/**
 * Created by dnlopes on 24/03/15.
 */
public class UniqueConstraint extends AbstractConstraint
{

	private final boolean isPrimaryKey;
	private DataField fieldToChange;

	public UniqueConstraint(boolean requiresCoordination, boolean isPrimaryKey)
	{
		super(ConstraintType.UNIQUE, requiresCoordination);

		this.isPrimaryKey = isPrimaryKey;
		this.fieldToChange = null;
	}

	@Override
	public void addField(DataField field)
	{
		super.addField(field);

		if(fieldToChange == null)
			if(field.isStringField() || field.isNumberField())
			{
				this.fieldToChange = field;
				this.fieldToChange.setInternallyChanged(true);
			}
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}

	public DataField getFieldToChange()
	{
		return this.fieldToChange;
	}

}
