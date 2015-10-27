package common.database.field.hidden;


import common.database.field.CrdtDataFieldType;
import common.database.field.DataField;
import common.database.util.SemanticPolicy;

import java.sql.ResultSet;


public class LogicalClockField extends DataField
{

	private static final String FIELD_TYPE = "string";

	public LogicalClockField(String tableName, int position, String fieldName)
	{
		super(CrdtDataFieldType.LWWLOGICALTIMESTAMP, fieldName, tableName, FIELD_TYPE, false, false,
				position, SemanticPolicy.NOSEMANTIC);
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

	@Override
	public boolean isStringField()
	{
		return true;
	}

	@Override
	public boolean isHiddenField()
	{
		return true;
	}
}
