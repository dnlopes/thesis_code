package database.util;


/**
 * Created by dnlopes on 11/05/15.
 */
public class FieldValue
{

	private String value;
	private String fieldName;
	private CrdtDataFieldType fieldType;

	public FieldValue(CrdtDataFieldType type, String fieldName, String value)
	{
		this.value = value;
		this.fieldName = fieldName;
		this.fieldType = type;
	}

	public String getValue()
	{
		if(this.fieldType == CrdtDataFieldType.LWWSTRING || this.fieldType == CrdtDataFieldType.NORMALSTRING)
			return "'" + this.value + "'";
		else if (this.fieldType == CrdtDataFieldType.NUMDELTADOUBLE || this.fieldType == CrdtDataFieldType
				.NUMDELTAFLOAT || this.fieldType == CrdtDataFieldType.NUMDELTAINTEGER)
			return this.fieldName + "+" + this.value;
		else
			return this.value;
	}

	public void setValue(String newValue)
	{
		this.value = newValue;
	}

	public String getFieldName()
	{
		return this.fieldName;
	}
}
