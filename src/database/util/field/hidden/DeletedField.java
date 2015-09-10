package database.util.field.hidden;


import database.util.field.CrdtDataFieldType;
import database.util.field.DataField;
import database.util.SemanticPolicy;
import util.defaults.DatabaseDefaults;

import java.sql.ResultSet;


/**
 * The Class LWW_DELETEDFLAG.
 */
public class DeletedField extends DataField
{

	private static final String FIELD_NAME = DatabaseDefaults.DELETED_COLUMN;
	private static final String FIELD_TYPE = "boolean";


	public DeletedField(String tableName, int position)
	{
		super(CrdtDataFieldType.LWWDELETEDFLAG, FIELD_NAME, tableName, FIELD_TYPE, false, false,
				position, SemanticPolicy.NOSEMANTIC);
	}

	/**
	 * @param rs
	 * @param Value
	 *
	 * @return
	 *
	 * @see database.util.field.DataField#get_Crdt_Form(java.sql.ResultSet, String)
	 */
	@Override
	public String get_Crdt_Form(ResultSet rs, String Value)
	{
		return null;
	}

	/**
	 * @param Value
	 *
	 * @return
	 *
	 * @see database.util.field.DataField#get_Crdt_Form(String)
	 */
	@Override
	public String get_Crdt_Form(String Value)
	{
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
	 * @see database.util.field.DataField#get_Value_In_Correct_Format(String)
	 */
	@Override
	public String get_Value_In_Correct_Format(String Value)
	{
		return Value;
	}

	/**
	 * @return
	 *
	 * @see database.util.field.DataField#getDefaultValue()
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

	@Override
	public boolean isHiddenField()
	{
		return true;
	}
}
