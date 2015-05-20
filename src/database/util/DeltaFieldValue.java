package database.util;


import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 16/05/15.
 */
public class DeltaFieldValue extends FieldValue
{

	private double delta;

	public DeltaFieldValue(DataField field, String value, String oldValue)
	{
		super(field, value);
		this.delta = 0;
		this.transformValueForDeltaField(oldValue);
	}

	private void transformValueForDeltaField(String oldField)
	{
		double oldValue = Double.parseDouble(oldField);

		if(this.value.contains(" "))
		{
			if(this.value.contains("+"))
			{
				String[] splitted = this.value.split("\\+");

				for(int i = 0; i < splitted.length; i++)
					splitted[i] = splitted[i].trim();

				if(splitted.length != 2)
					RuntimeHelper.throwRunTimeException("malformed delta update field", ExitCode.INVALIDUSAGE);

				if(this.dataField.getFieldName().compareTo(splitted[0]) == 0) //[0] is the fieldName
					this.delta = Double.parseDouble(splitted[1]);
				else //[1] is the fieldName
					this.delta = Double.parseDouble(splitted[0]);

			} else if(this.value.contains("-"))
			{
				String[] splitted = this.value.split("-");

				for(int i = 0; i < splitted.length; i++)
					splitted[i] = splitted[i].trim();

				if(splitted.length != 2)
					RuntimeHelper.throwRunTimeException("malformed delta update field", ExitCode.INVALIDUSAGE);

				if(this.dataField.getFieldName().compareTo(splitted[0]) == 0) //[0] is the fieldName
					this.delta = Double.parseDouble(splitted[1]);
				else //[1] is the fieldName
					this.delta = Double.parseDouble(splitted[0]);

				this.delta *= -1;
			}
		}

		this.value = String.valueOf(oldValue + this.delta);
	}

	@Override
	public String getFormattedValue()
	{
		return this.dataField.getFieldName() + "+" + this.delta;
	}
}
