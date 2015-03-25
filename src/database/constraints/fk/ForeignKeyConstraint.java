package database.constraints.fk;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import database.util.DataField;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dnlopes on 24/03/15.
 */
public class ForeignKeyConstraint extends AbstractConstraint implements IForeignKeyConstraint
{

	private List<String> remoteFields;
	private String remoteTable;

	public ForeignKeyConstraint()
	{
		super(ConstraintType.FOREIGN_KEY);
		this.remoteFields = new ArrayList<>();
	}
	
	@Override
	public void addPair(DataField field, String remoteField)
	{
		this.fields.add(fields.size(), field);
		this.remoteFields.add(this.remoteFields.size(), remoteField);
	}

	@Override
	public void setRemoteTable(String table)
	{
		this.remoteTable = table;
	}

	@Override
	public String getRemoteTable()
	{
		return this.remoteTable;
	}
}