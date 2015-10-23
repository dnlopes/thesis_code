package database.util.field;


import java.sql.ResultSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import database.constraints.Constraint;
import database.constraints.check.CheckConstraint;
import database.constraints.fk.ForeignKeyConstraint;
import database.constraints.unique.AutoIncrementConstraint;
import database.constraints.unique.UniqueConstraint;
import database.util.SemanticPolicy;
import database.util.table.DatabaseTable;
import database.util.value.FieldValue;
import runtime.RuntimeUtils;
import util.ExitCode;


/**
 * The Class DataField.
 */
public abstract class DataField
{

	private List<Constraint> invariants;
	private List<CheckConstraint> checkConstraints;
	private List<UniqueConstraint> uniqueConstraints;
	private List<ForeignKeyConstraint> fkConstraints;
	private AutoIncrementConstraint autoIncrementConstraint;
	private Set<DataField> childFields;
	private Set<DataField> parentsFields;
	private CrdtDataFieldType crdtDataType;
	private SemanticPolicy semantic;
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
	private boolean isUnique;
	private FieldValue defaultFieldValue;

	protected DataField(CrdtDataFieldType fieldTag, String name, String tableName, String fieldType,
						boolean isPrimaryKey, boolean isAutoIncremental, int pos, SemanticPolicy semanticPolicy)
	{

		this.invariants = new LinkedList<>();
		this.checkConstraints = new LinkedList<>();
		this.uniqueConstraints = new LinkedList<>();
		this.fkConstraints = new LinkedList<>();
		this.childFields = new HashSet<>();
		this.parentsFields = new HashSet<>();
		this.isAllowedNULL = false;
		this.isUnique = false;
		this.position = -1;
		this.crdtDataType = fieldTag;
		this.fieldName = name;
		this.tableName = tableName;
		this.dataType = fieldType;
		this.isPrimaryKey = isPrimaryKey;
		this.isAutoIncremental = isAutoIncremental;
		this.position = pos;
		this.semantic = semanticPolicy;

		this.autoIncrementConstraint = null;
		this.defaultValue = null;

		if(this.isAutoIncremental)
		{
			this.isUnique = true;

			boolean requiresCoordination = this.semantic == SemanticPolicy.SEMANTIC;

			Constraint autoIncrementConstraint = new AutoIncrementConstraint(requiresCoordination);
			autoIncrementConstraint.setTableName(this.dbTable.getName());
			autoIncrementConstraint.addField(this);
			autoIncrementConstraint.generateIdentifier();
			addConstraint(autoIncrementConstraint);
		}
	}

	public CrdtDataFieldType getCrdtType()
	{
		return this.crdtDataType;
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public void setDefaultValue(String value)
	{
		this.defaultValue = value;
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

	public List<Constraint> getAllConstraints()
	{
		return this.invariants;
	}

	public void addConstraint(Constraint inv)
	{
		if(inv instanceof CheckConstraint)
			this.checkConstraints.add((CheckConstraint) inv);
		else if(inv instanceof UniqueConstraint)
			this.uniqueConstraints.add((UniqueConstraint) inv);
		else if(inv instanceof ForeignKeyConstraint)
			this.fkConstraints.add((ForeignKeyConstraint) inv);
		else if(inv instanceof AutoIncrementConstraint)
		{
			if(autoIncrementConstraint != null)
				RuntimeUtils.throwRunTimeException(
						"multiple auto_increment constraints on the same field now " + "allowed",
						ExitCode.DUPLICATED_FIELD);

			this.autoIncrementConstraint = (AutoIncrementConstraint) inv;
		}

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

	public void setIsUnique()
	{
		this.isUnique = true;
	}

	public SemanticPolicy getSemantic()
	{
		return this.semantic;
	}

	public boolean isUnique()
	{
		return this.isUnique;
	}

	public FieldValue getDefaultFieldValue()
	{
		return this.defaultFieldValue;
	}

	public void setDefaultFieldValue(FieldValue defaultFieldValue)
	{
		this.defaultFieldValue = defaultFieldValue;
	}

	public List<CheckConstraint> getCheckConstraints()
	{
		return this.checkConstraints;
	}

	public List<UniqueConstraint> getUniqueConstraints()
	{
		return this.uniqueConstraints;
	}

	public List<ForeignKeyConstraint> getFkConstraints()
	{
		return this.fkConstraints;
	}

	public AutoIncrementConstraint getAutoIncrementConstraint()
	{
		return this.autoIncrementConstraint;
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

	public String getFieldName()
	{
		return this.fieldName;
	}

	public boolean isStringField()
	{
		return false;
	}

	public String getDefaultValue()
	{
		return this.defaultValue;
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

	public boolean isDateField()
	{
		return false;
	}

	public boolean isNumberField()
	{
		return false;
	}

	public abstract String get_Crdt_Form(ResultSet rs, String Value);

	public abstract String get_Crdt_Form(String Value);

	public abstract String get_Value_In_Correct_Format(String Value);

}
