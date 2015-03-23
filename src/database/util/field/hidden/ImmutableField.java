package database.util.field.hidden;


import database.util.CrdtDataFieldType;
import database.util.DataField;
import util.defaults.ScratchpadDefaults;

import java.sql.ResultSet;


public class ImmutableField extends DataField
{

	private static final String FIELD_NAME = ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE;
	private static final String FIELD_TYPE = "int";

	public ImmutableField(String tableName, int position)
	{
		super(CrdtDataFieldType.IMMUTABLE_FIELD, FIELD_NAME, tableName, FIELD_TYPE, false, false, false, position);
	}

	@Override
	public String get_Crdt_Form(ResultSet rs, String Value)
	{
		return null;
	}

	@Override
	public String get_Crdt_Form(String Value)
	{
		return this.getFieldName() + " = " + Value;
	}

	@Override
	public String get_Value_In_Correct_Format(String Value)
	{
		return Value;
	}
}
