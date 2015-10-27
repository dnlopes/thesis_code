package common.database.value;


import common.database.util.DatabaseCommon;
import common.database.field.DataField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Created by dnlopes on 29/08/15.
 */
public class DateFieldValue extends FieldValue
{

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public DateFieldValue(DataField field, String value)
	{
		super(field, value);
	}

	@Override
	public String getFormattedValue()
	{
		return "'" + DatabaseCommon.CURRENTTIMESTAMP(DATE_FORMAT) + "'";
	}
}
