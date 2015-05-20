package database.util;


/**
 * Created by dnlopes on 11/05/15.
 */
public class FieldValue
{

	protected final DataField dataField;
	protected String value;

	public FieldValue(DataField field, String value)
	{
		this.dataField = field;
		this.value = value.trim().replace("'", "");
	}

	public String getValue()
	{
		return this.value;
	}

	public String getFormattedValue()
	{
		CrdtDataFieldType fieldType = this.dataField.getCrdtType();

		if(fieldType == CrdtDataFieldType.LWWDATETIME || fieldType == CrdtDataFieldType.NORMALDATETIME || fieldType ==
				CrdtDataFieldType.NUMDELTADATETIME || fieldType == CrdtDataFieldType.LWWSTRING || fieldType ==
				CrdtDataFieldType.NORMALSTRING ||
				fieldType == CrdtDataFieldType.LWWLOGICALTIMESTAMP)
			return "'" + this.value + "'";
		else
			return this.value;
	}

	public void setValue(String newValue)
	{
		this.value = newValue;
	}

	public DataField getDataField()
	{
		return this.dataField;
	}

	@Override
	public String toString()
	{
		return this.value;
	}
}
