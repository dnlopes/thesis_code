package database.util;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import database.invariants.Invariant;


/**
 * The Class DataField.
 */
public abstract class DataField
{

	private String originalDeclaration;
	private List<Invariant> invariants;
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


	protected DataField(CrdtDataFieldType fieldTag, String name, String tableName, String fieldType,
						boolean isPrimaryKey, boolean isForeignKey, boolean isAutoIncremental, int pos)
	{
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
		this.invariants = new LinkedList<>();
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

	public String getFieldType()
	{
		return this.dataType;
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

	public boolean isAutoIncrement()
	{
		return this.isAutoIncremental;
	}

	public int getPosition()
	{
		return this.position;
	}

	public List<Invariant> getInvariants()
	{
		return this.invariants;
	}

	public void addInvariant(Invariant inv)
	{
		this.invariants.add(inv);
	}

	public boolean hasInvariants()
	{
		return this.invariants.size() > 0;
	}

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

	public String getOriginalDeclaration()
	{
		return this.originalDeclaration;
	}

	public void setOriginalDeclaration(String decl)
	{
		this.originalDeclaration = decl;
	}

	public boolean isDeltaField()
	{
		return this.crdtDataType == CrdtDataFieldType.NUMDELTADATETIME || this.crdtDataType == CrdtDataFieldType
				.NUMDELTADOUBLE || this.crdtDataType == CrdtDataFieldType.NUMDELTAFLOAT || this.crdtDataType ==
				CrdtDataFieldType.NUMDELTAINTEGER;
	}
}
