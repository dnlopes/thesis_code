package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import database.util.field.DataField;


/**
 * Created by dnlopes on 24/03/15.
 */
public class UniqueConstraint extends AbstractConstraint
{

	private DataField fieldToChange;

	public UniqueConstraint(boolean requiresCoordination)
	{
		super(ConstraintType.UNIQUE, requiresCoordination);
		this.fieldToChange = null;
	}

	@Override
	public void addField(DataField field)
	{
		this.fields.add(field);
		this.fieldsMap.put(field.getFieldName(), field);

		if(fieldToChange == null)
			if(field.isStringField() || field.isNumberField())
			{
				this.fieldToChange = field;
				this.fieldToChange.setInternallyChanged(true);
			}
	}

	public DataField getFieldToChange()
	{
		return this.fieldToChange;
	}

}
