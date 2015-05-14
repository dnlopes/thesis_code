package database.util;


import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 11/05/15.
 */
public class FieldValue
{

	private DataField dataField;
	private String value;

	public FieldValue(DataField field, String value)
	{
		this.dataField = field;
		this.value = value.trim();
	}

	public String getValue()
	{
		return this.value;
	}

	public String getFormattedValue()
	{
		CrdtDataFieldType fieldType = this.dataField.getCrdtType();

		if(this.dataField.isDeltaField())
			return this.dataField.getFieldName() + "+" + this.value;
		else if(fieldType == CrdtDataFieldType.LWWSTRING || fieldType == CrdtDataFieldType.NORMALSTRING)
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

	public void transformValueForDeltaField(FieldValue oldField)
	{
		if(this.value.contains("+"))
		{
			String[] splitted = this.value.split("\\+");

			for(int i = 0; i < splitted.length; i++)
				splitted[i] = splitted[i].trim();

			if(splitted.length != 2)
				RuntimeHelper.throwRunTimeException("malformed delta update field", ExitCode.INVALIDUSAGE);

			double newValue;

			if(this.dataField.getFieldName().compareTo(splitted[0]) == 0) //[0] is the fieldName
				newValue = Double.parseDouble(splitted[1]);
			else //[1] is the fieldName
				newValue = Double.parseDouble(splitted[0]);

			double oldValue = Double.parseDouble(oldField.getValue());

			double finalValue = oldValue + newValue;
			this.value = String.valueOf(finalValue);

		} else if(this.value.contains("-"))
		{
			String[] splitted = this.value.split("-");

			for(int i = 0; i < splitted.length; i++)
				splitted[i] = splitted[i].trim();

			if(splitted.length != 2)
				RuntimeHelper.throwRunTimeException("malformed delta update field", ExitCode.INVALIDUSAGE);

			double newValue;

			if(this.dataField.getFieldName().compareTo(splitted[0]) == 0) //[0] is the fieldName
				newValue = Double.parseDouble(splitted[1]);
			else //[1] is the fieldName
				newValue = Double.parseDouble(splitted[0]);

			double oldValue = Double.parseDouble(oldField.getValue());

			double deltaValue = oldValue - newValue;
			this.value = String.valueOf(deltaValue);
		}
	}

	@Override
	public String toString()
	{
		return this.value;
	}
}
