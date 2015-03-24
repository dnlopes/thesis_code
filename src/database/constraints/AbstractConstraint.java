package database.constraints;


import database.util.DataField;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 06/03/15.
 */
public abstract class AbstractConstraint implements Constraint
{

	protected List<DataField> fields;
	protected ConstraintType constraintType;

	public AbstractConstraint(ConstraintType type)
	{
		this.constraintType = type;
		this.fields = new ArrayList<>();
	}

	@Override
	public ConstraintType getType()
	{
		return this.constraintType;
	}

	@Override
	public List<DataField> getFields()
	{
		return this.fields;
	}

	@Override
	public void addField(DataField field)
	{
		this.fields.add(field);
	}
}
