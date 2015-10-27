package common.database.field.hidden;


import common.database.field.CrdtDataFieldType;
import common.database.field.DataField;
import common.database.util.SemanticPolicy;
import common.util.defaults.ScratchpadDefaults;

import java.sql.ResultSet;


public class LWWField extends DataField
{

	/** The Constant logical_Timestamp_Name. */
	private static final String FIELD_NAME = ScratchpadDefaults.SCRATCHPAD_COL_TS;
	private static final String FIELD_TYPE = "int";

	public LWWField(String tableName, int position)
	{
		super(CrdtDataFieldType.LWWINTEGER, FIELD_NAME, tableName, FIELD_TYPE, false, false, position, SemanticPolicy.NOSEMANTIC);
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

	public String get_Set_Timestamp_LWW() {
		return FIELD_NAME + " = ?";
	}

	/**
	 * Gets the _ set_ lw w_ clause.
	 *
	 * @return the _ set_ lw w_ clause
	 */
	public String get_Set_LWW_Clause() {
		return FIELD_NAME + " <= ?";
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @return the _ data_ field_ name
	 */
	public String get_Data_Field_Name() {
		return FIELD_NAME;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString() {
		return "LWW_Timestamp: " + FIELD_NAME;
	}
}
