package database.util;


/**
 * Created by dnlopes on 11/05/15.
 */
public class FieldValue
{

	private DataField dataField;
	private String value;
	private CrdtDataFieldType fieldType;


	public FieldValue(DataField field, String value)
	{
		this.dataField = field;
		this.value = value;
		this.fieldType = this.dataField.getCrdtType();
	}

	public String getValue()
	{
		return this.value;
		/*
		if(this.fieldType == CrdtDataFieldType.LWWSTRING || this.fieldType == CrdtDataFieldType.NORMALSTRING)
			return "'" + this.value + "'";
		else if (this.fieldType == CrdtDataFieldType.NUMDELTADOUBLE || this.fieldType == CrdtDataFieldType
				.NUMDELTAFLOAT || this.fieldType == CrdtDataFieldType.NUMDELTAINTEGER)
			return this.dataField.getFieldName() + "+" + this.value;
		else
			return this.value;               */
	}

	public void setValue(String newValue)
	{
		this.value = newValue;
	}

	public DataField getDataField()
	{
		return this.dataField;
	}
}
