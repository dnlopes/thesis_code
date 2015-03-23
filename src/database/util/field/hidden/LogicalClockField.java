package database.util.field.hidden;


import database.util.CrdtDataFieldType;
import database.util.DataField;
import util.defaults.ScratchpadDefaults;

import java.sql.ResultSet;


public class LogicalClockField extends DataField
{

	/** The Constant logical_Timestamp_Name. */
	private static final String FIELD_NAME = ScratchpadDefaults.SCRATCHPAD_COL_VV;
	private static final String FIELD_TYPE = "string";

	public LogicalClockField(String tableName, int position)
	{
		super(CrdtDataFieldType.LWWLOGICALTIMESTAMP, FIELD_NAME, tableName, FIELD_TYPE, false, false, false, position);
	}

	@Override
	public String get_Crdt_Form(ResultSet rs, String Value)
	{
		return null;
	}

	@Override
	public String get_Crdt_Form(String Value)
	{
		Value = Value.trim();
		if(Value.indexOf("'") == 0 && Value.lastIndexOf("'") == (Value.length() - 1))
			Value = Value.substring(1, Value.length() - 1);
		return this.getFieldName() + " = '" + Value + "'";
	}

	@Override
	public String get_Value_In_Correct_Format(String Value)
	{
		if(Value.indexOf("'") == 0 && Value.lastIndexOf("'") == Value.length() - 1)
			return Value;
		return "'" + Value + "'";
	}

	public String get_Set_Logical_Timestamp(String value)
	{
		return FIELD_NAME + "= '" + value + "' ";
	}

	public String get_Set_Logical_Timestamp()
	{
		return FIELD_NAME + "= ?";
	}

	public String getFieldName()
	{
		return FIELD_NAME;
	}
}
