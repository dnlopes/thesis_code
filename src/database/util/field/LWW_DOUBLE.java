package database.util.field;

import java.sql.ResultSet;

import database.util.SemanticPolicy;

// TODO: Auto-generated Javadoc


/**
 * The Class LWW_DOUBLE.
 */
public class LWW_DOUBLE extends DataField
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
	public LWW_DOUBLE(String dFN, String tN, String dT, boolean iPK, boolean iAIC, int position, SemanticPolicy policy)
	{
		super(CrdtDataFieldType.LWWDOUBLE, dFN, tN, dT, iPK, iAIC, position, policy);
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
	 */

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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see crdts.basics.Data_Field#get_Crdt_Form(java.lang.String)
	 */

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
		// TODO Auto-generated method stub
		return this.getFieldName() + " = " + Value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Data_Field#get_Value_In_Correct_Format(java.lang.String)
	 */

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
		// TODO Auto-generated method stub
		return Value;
	}

	@Override
	public boolean isNumberField()
	{
		return true;
	}
}
