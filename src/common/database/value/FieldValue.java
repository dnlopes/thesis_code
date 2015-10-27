package common.database.value;


import common.database.field.CrdtDataFieldType;
import common.database.field.DataField;


/**
 * Created by dnlopes on 11/05/15.
 */
public class FieldValue
{

	protected final DataField dataField;
	protected String value;
	private static final String NULL_VALUE = "NULL";

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
		if(this.value.compareTo(NULL_VALUE) == 0)
			return this.value;

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
