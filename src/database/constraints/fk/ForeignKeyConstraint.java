package database.constraints.fk;


import database.constraints.AbstractConstraint;
import database.constraints.ConstraintType;
import database.util.field.DataField;
import database.util.table.CRDTTableType;
import database.util.table.DatabaseTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 */
public class ForeignKeyConstraint extends AbstractConstraint implements IForeignKeyConstraint
{

	private ForeignKeyPolicy policy;
	private List<String> parentFieldsNames;
	private List<DataField> parentFields;
	private List<ParentChildRelation> relationsList;
	private Map<DataField, ParentChildRelation> relationsMap;
	private DatabaseTable parentTable;
	private DatabaseTable childTable;

	public ForeignKeyConstraint(ForeignKeyPolicy policy)
	{
		super(ConstraintType.FOREIGN_KEY, false);
		this.policy = policy;
		this.parentFieldsNames = new ArrayList<>();
		this.parentFields = new ArrayList<>();
		this.relationsList = new ArrayList<>();
		this.relationsMap = new HashMap<>();
	}

	public void addRemoteField(DataField originalField)
	{
		this.parentFields.add(originalField);
	}

	public void addParentChildRelation(DataField parent, DataField child)
	{
		ParentChildRelation relation = new ParentChildRelation(parent, child);
		parent.addChildField(child);
		child.addParentField(parent);
		this.relationsList.add(relation);
		this.relationsMap.put(child, relation);
	}

	@Override
	public void setParentTable(DatabaseTable table)
	{
		this.parentTable = table;
	}

	@Override
	public void setChildTable(DatabaseTable childTable)
	{
		this.childTable = childTable;
	}

	@Override
	public DatabaseTable getParentTable()
	{
		return this.parentTable;
	}

	public List<String> getParentFields()
	{
		return this.parentFieldsNames;
	}

	public ForeignKeyPolicy getPolicy()
	{
		return this.policy;
	}

	public DataField getMatchingParent(DataField child)
	{
		return this.relationsMap.get(child).getParent();
	}

	public List<ParentChildRelation> getFieldsRelations()
	{
		return this.relationsList;
	}

	public DatabaseTable getChildTable()
	{
		return this.childTable;
	}

	public boolean requiresParentConsistency()
	{
		return this.parentTable.getTableType() == CRDTTableType.ARSETTABLE;
	}

}
