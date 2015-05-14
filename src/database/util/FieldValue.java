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

	public void transformValueForDeltaField(FieldValue oldField)
	{
		if(this.value.contains("+"))
		{
			String[] splitted = this.value.split("\\+");

			for(String elem : splitted)
				elem = elem.trim();

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

			for(String elem : splitted)
				elem = elem.trim();

			if(splitted.length != 2)
				RuntimeHelper.throwRunTimeException("malformed delta update field", ExitCode.INVALIDUSAGE);

			double newValue;

			if(this.dataField.getFieldName().compareTo(splitted[0]) == 0) //[0] is the fieldName
				newValue = Double.parseDouble(splitted[1]);
			else //[1] is the fieldName
				newValue = Double.parseDouble(splitted[0]);

			double oldValue = Double.parseDouble(oldField.getValue());

			double finalValue = oldValue - newValue;
			this.value = String.valueOf(finalValue);
		}
	}

	@Override
	public String toString()
	{
		return this.value;
	}
}
