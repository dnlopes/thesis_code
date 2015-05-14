package database.util.field.hidden;


import database.util.CrdtDataFieldType;
import database.util.DataField;
import util.defaults.ScratchpadDefaults;

import java.sql.ResultSet;


/**
 * The Class LWW_DELETEDFLAG.
 */
public class DeletedField extends DataField
{

	private static final String FIELD_NAME = ScratchpadDefaults.SCRATCHPAD_COL_DELETED;
	private static final String FIELD_TYPE = "boolean";


	public DeletedField(String tableName, int position)
	{
		super(CrdtDataFieldType.LWWDELETEDFLAG, FIELD_NAME, tableName, FIELD_TYPE, false, false, false,
				position, true);
	}

	/**
	 * @param rs
	 * @param Value
	 *
	 * @return
	 *
	 * @see database.util.DataField#get_Crdt_Form(java.sql.ResultSet, String)
	 */
	@Override
	public String get_Crdt_Form(ResultSet rs, String Value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param Value
	 *
	 * @return
	 *
	 * @see database.util.DataField#get_Crdt_Form(String)
	 */
	@Override
	public String get_Crdt_Form(String Value)
	{
		// TODO Auto-generated method stub
		Value = Value.trim();
		if(Value.indexOf("'") == 0 && Value.lastIndexOf("'") == (Value.length() - 1))
			Value = Value.substring(1, Value.length() - 1);
		return this.getFieldName() + " = '" + Value + "'";
	}

	/**
	 * @param Value
	 *
	 * @return
	 *
	 * @see database.util.DataField#get_Value_In_Correct_Format(String)
	 */
	@Override
	public String get_Value_In_Correct_Format(String Value)
	{
		// TODO Auto-generated method stub
		if(Value.indexOf("'") == 0 && Value.lastIndexOf("'") == Value.length() - 1)
			return Value;
		return "'" + Value + "'";
	}

	/**
	 * @return
	 *
	 * @see database.util.DataField#getDefaultValue()
	 */
	public String getDefaultValue()
	{
		return "false";
	}

	@Override
	public boolean isDeletedFlagField()
	{
		return true;
	}
}
