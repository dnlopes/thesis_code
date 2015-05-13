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
	private String constraintIdentifier;
	private String tableName;

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

	@Override
	public String getConstraintIdentifier()
	{
		return this.constraintIdentifier;
	}

	@Override
	public void setTableName(String name)
	{
		this.tableName = name;
	}

	@Override
	public void generateIdentifier()
	{
		StringBuilder buffer = new StringBuilder();

		for(DataField field : fields)
		{
			buffer.append(field.getFieldName());
			buffer.append("_");
		}

		buffer.append(this.tableName);
		buffer.append("_");
		buffer.append(this.constraintType);
		this.constraintIdentifier = buffer.toString();
	}

	@Override
	public String getTableName()
	{
		return this.tableName;
	}
}
