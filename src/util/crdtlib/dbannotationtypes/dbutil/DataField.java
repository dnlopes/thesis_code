package util.crdtlib.dbannotationtypes.dbutil;

import java.sql.ResultSet;

import util.crdtlib.dbannotationtypes.CrdtFactory;

// TODO: Auto-generated Javadoc


/**
 * The Class DataField.
 */
public abstract class DataField
{

	/** The crdt data type. */
	CrdtDataFieldType crdtDataType;

	/** The data field name. */
	String fieldName;
	/** The table name. */
	String tableName;
	/** The data type. */
	String dataType;
	/** The default value. */
	String defaultValue = null;
	/** The is primary key. */
	boolean isPrimaryKey;
	/** The is foreign key. */
	boolean isForeignKey;
	/** The is auto incremental. */
	boolean isAutoIncremental;
	/** The is allowed null. */
	boolean isAllowedNULL = false;
	/** The position. */
	int position = - 1;

	/**
	 * Instantiates a new data field.
	 *
	 * @param crdtFieldType     the c dt
	 * @param name              the d fn
	 * @param tableName         the t n
	 * @param fieldType         the d t
	 * @param isPrimaryKey      the i pk
	 * @param isForeignKey      the i fk
	 * @param isAutoIncremental the i aic
	 * @param pos               the pos
	 */
	protected DataField(CrdtDataFieldType crdtFieldType, String name, String tableName, String fieldType, boolean isPrimaryKey, boolean isForeignKey, boolean isAutoIncremental, int pos)
	{
		this.crdtDataType = crdtFieldType;
		this.fieldName = name;
		this.tableName = tableName;
		this.dataType = fieldType;
		this.isPrimaryKey = isPrimaryKey;
		this.isForeignKey = isForeignKey;
		this.isAutoIncremental = isAutoIncremental;
		this.position = pos;
	}

	/**
	 * Gets the _ crdt_ form.
	 *
	 * @param rs    the rs
	 * @param Value the value
	 *
	 * @return the _ crdt_ form
	 */
	public abstract String get_Crdt_Form(ResultSet rs, String Value);

	/**
	 * Gets the _ crdt_ form.
	 *
	 * @param Value the value
	 *
	 * @return the _ crdt_ form
	 */
	public abstract String get_Crdt_Form(String Value);

	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param Value the value
	 *
	 * @return the _ value_ in_ correct_ format
	 */
	public abstract String get_Value_In_Correct_Format(String Value);

	/**
	 * Gets the _ crdt_ data_ type.
	 *
	 * @return the _ crdt_ data_ type
	 */
	public CrdtDataFieldType getCrdtType()
	{
		return this.crdtDataType;
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @return the _ data_ field_ name
	 */
	public String getFieldName()
	{
		return this.fieldName;
	}

	/**
	 * Gets the _ table_ name.
	 *
	 * @return the _ table_ name
	 */
	public String getTableName()
	{
		return this.tableName;
	}

	/**
	 * Gets the _ data_ type.
	 *
	 * @return the _ data_ type
	 */
	public String getFieldType()
	{
		return this.dataType;
	}

	/**
	 * Sets the _ default_ value.
	 *
	 * @param value the new _ default_ value
	 */
	public void setDefaultValue(String value)
	{
		this.defaultValue = value;
	}

	/**
	 * Set_ nul l_ default_ value.
	 */
	public void set_NULL_Default_Value()
	{
		this.setDefaultValue("NULL");
		this.isAllowedNULL = true;
	}

	/**
	 * Gets the _ default_ value.
	 *
	 * @return the _ default_ value
	 */
	public String getDefaultValue()
	{
		return this.defaultValue;
	}

	/**
	 * Checks if is _ primary_ key.
	 *
	 * @return true, if is _ primary_ key
	 */
	public boolean isPrimaryKey()
	{
		return this.isPrimaryKey;
	}

	/**
	 * Set_ primary_ key.
	 */
	public void setPrimaryKey()
	{
		this.isPrimaryKey = true;
	}

	/**
	 * Checks if is _ foreign_ key.
	 *
	 * @return true, if is _ foreign_ key
	 */
	public boolean isForeignKey()
	{
		return this.isForeignKey;
	}

	/**
	 * Set_ foreign_ key.
	 */
	public void setForeignKey()
	{
		this.isForeignKey = true;
	}

	/**
	 * Checks if is _ auto increment.
	 *
	 * @return true, if is _ auto increment
	 */
	public boolean isAutoIncrement()
	{
		return this.isAutoIncremental;
	}

	/**
	 * Checks if is _ allowed null.
	 *
	 * @return true, if is _ allowed null
	 */
	public boolean isAllowedNULL()
	{
		return this.isAllowedNULL;
	}

	/**
	 * Gets the _ position.
	 *
	 * @return the _ position
	 */
	public int getPosition()
	{
		return this.position;
	}

	/**
	 * Checks if is not normal data type.
	 *
	 * @return true, if is not normal data type
	 */
	public boolean isNotNormalDataType()
	{
		return ! CrdtFactory.isNormalDataType(this.crdtDataType);

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */

	/**
	 * To string.
	 *
	 * @return the string
	 *
	 * @see java.lang.Object#toString()
	 */
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
