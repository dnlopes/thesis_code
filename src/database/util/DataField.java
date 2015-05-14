package database.util;


import java.sql.ResultSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import database.constraints.Constraint;


/**
 * The Class DataField.
 */
public abstract class DataField
{

	private List<Constraint> invariants;
	private Set<DataField> childFields;
	private Set<DataField> parentsFields;
	private CrdtDataFieldType crdtDataType;
	private String fieldName;
	private String tableName;
	private String dataType;
	private String defaultValue;
	private boolean isPrimaryKey;
	private boolean isForeignKey;
	private boolean isAutoIncremental;
	private boolean isAllowedNULL;
	private int position;
	private DatabaseTable dbTable;

	protected DataField(CrdtDataFieldType fieldTag, String name, String tableName, String fieldType,
						boolean isPrimaryKey, boolean isForeignKey, boolean isAutoIncremental, int pos,
						boolean isHiddenField)
	{

		this.invariants = new LinkedList<>();
		this.childFields = new HashSet<>();
		this.parentsFields = new HashSet<>();

		this.defaultValue = null;
		this.isAllowedNULL = false;
		this.position = -1;
		this.crdtDataType = fieldTag;
		this.fieldName = name;
		this.tableName = tableName;
		this.dataType = fieldType;
		this.isPrimaryKey = isPrimaryKey;
		this.isForeignKey = isForeignKey;
		this.isAutoIncremental = isAutoIncremental;
		this.position = pos;
	}

	public abstract String get_Crdt_Form(ResultSet rs, String Value);

	public abstract String get_Crdt_Form(String Value);

	public abstract String get_Value_In_Correct_Format(String Value);

	public CrdtDataFieldType getCrdtType()
	{
		return this.crdtDataType;
	}

	public String getFieldName()
	{
		return this.fieldName;
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public void setDefaultValue(String value)
	{
		this.defaultValue = value;
	}

	public String getDefaultValue()
	{
		return this.defaultValue;
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}

	public void setPrimaryKey()
	{
		this.isPrimaryKey = true;
	}

	public boolean isForeignKey()
	{
		return this.isForeignKey;
	}

	public void setForeignKey()
	{
		this.isForeignKey = true;
	}

	public boolean isStringField()
	{
		return false;
	}

	public boolean isAutoIncrement()
	{
		return this.isAutoIncremental;
	}

	public int getPosition()
	{
		return this.position;
	}

	public List<Constraint> getInvariants()
	{
		return this.invariants;
	}

	public void addInvariant(Constraint inv)
	{
		this.invariants.add(inv);
	}

	public boolean isImmutableField()
	{
		return this.crdtDataType == CrdtDataFieldType.IMMUTABLE_FIELD || this.crdtDataType == CrdtDataFieldType
				.NORMALBOOLEAN || this.crdtDataType == CrdtDataFieldType.NORMALDATETIME || this.crdtDataType ==
				CrdtDataFieldType.NORMALDOUBLE || this.crdtDataType == CrdtDataFieldType.NORMALFLOAT || this
				.crdtDataType == CrdtDataFieldType.NORMALINTEGER || this.crdtDataType == CrdtDataFieldType
				.NORMALSTRING;
	}

	public boolean isDeletedFlagField()
	{
		return false;
	}

	public boolean isDeltaField()
	{
		return false;
	}

	public boolean isHiddenField()
	{
		return false;
	}

	public void setDatabaseTable(DatabaseTable table)
	{
		this.dbTable = table;
	}

	public DatabaseTable getTable()
	{
		return this.dbTable;
	}

	public void addChildField(DataField field)
	{
		this.childFields.add(field);
	}

	public boolean hasChilds()
	{
		return this.childFields.size() > 0;
	}

	public void addParentField(DataField parent)
	{
		this.parentsFields.add(parent);
	}

	@Override
	public String toString()
	{
		String status = " TableName: " + this.tableName + "\n";
		status += " DataFieldName: " + this.fieldName + "\n";
		status += " DataType: " + this.dataType + "\n";
		status += " PrimaryKey: " + this.isPrimaryKey + "\n";
		status += " ForeignKey: " + this.isForeignKey + "\n";
		status += " AutoIncremental: " + this.isAutoIncremental + "\n";
		status += " IsAllowedNULL: " + this.isAllowedNULL + "\n";
		if(this.isAllowedNULL)
		{
			status += " DefaultValue: " + this.defaultValue + "\n";
		}
		status += " Position: " + this.position + "\n";
		status += " CrdtType: " + this.crdtDataType + "\n";
		return status;
	}

}
