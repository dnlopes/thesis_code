package common.database.field;

import java.sql.ResultSet;

import common.database.util.SemanticPolicy;


/**
 * The Class LWW_DOUBLE.
 */
public class LWW_BOOLEAN extends DataField
{

	/**
	 * Instantiates a new lww double.
	 *
	 * @param dFN      the d fn
	 * @param tN       the t n
	 * @param dT       the d t
	 * @param iPK      the i pk
	 * @param iFK      the i fk
	 * @param iAIC     the i aic
	 * @param position the position
	 */
	public LWW_BOOLEAN(String dFN, String tN, String dT, boolean iPK, boolean iAIC, int position, SemanticPolicy policy)
	{
		super(CrdtDataFieldType.LWWBOOLEAN, dFN, tN, dT, iPK, iAIC, position, policy);
	}

	/**
	 * @param rs
	 * @param Value
	 *
	 * @return
	 *
	 * @see DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
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
	 * @see DataField#get_Crdt_Form(java.lang.String)
	 */
	@Override
	public String get_Crdt_Form(String Value)
	{
		return this.getFieldName() + " = " + Value;
	}

	/**
	 * @param Value
	 *
	 * @return
	 *
	 * @see DataField#get_Value_In_Correct_Format(java.lang.String)
	 */
	@Override
	public String get_Value_In_Correct_Format(String Value)
	{
		return Value;
	}
}
