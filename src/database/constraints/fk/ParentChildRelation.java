package database.constraints.fk;


import database.util.DataField;


/**
 * Created by dnlopes on 12/05/15.
 */
public class ParentChildRelation
{

	private final DataField parent;
	private final DataField child;

	public ParentChildRelation(DataField parent, DataField child)
	{
		this.parent = parent;
		this.child = child;
	}

	public DataField getParent()
	{
		return this.parent;
	}

	public DataField getChild()
	{
		return this.child;
	}
	
}
