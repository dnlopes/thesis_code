package database.constraints.unique;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import database.util.field.DataField;


/**
 * Created by dnlopes on 24/03/15.
 */
public class AutoIncrementConstraint extends AbstractConstraint
{

	private DataField autoIncrementField;

	public AutoIncrementConstraint(boolean requiresCoordination)
	{
		super(ConstraintType.AUTO_INCREMENT, requiresCoordination);
		this.autoIncrementField = null;
	}

	@Override
	public void addField(DataField field)
	{
		this.fields.add(field);
		this.fieldsMap.put(field.getFieldName(), field);

		if(this.autoIncrementField == null)
			this.autoIncrementField = field;
	}

	public DataField getAutoIncrementField()
	{
		return this.autoIncrementField;
	}
}