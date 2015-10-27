package common.database.field;

import java.sql.ResultSet;

import common.database.util.SemanticPolicy;
import common.util.ExitCode;

/**
 * The Class NONCRDT_Data_Field.
 */
public class NONCRDT_Data_Field extends DataField {

	/**
	 * Instantiates a new nONCRD t_ data_ field.
	 *
	 * @param dFN the d fn
	 * @param tN the t n
	 * @param dT the d t
	 * @param iPK the i pk
	 * @param iFK the i fk
	 * @param iAIC the i aic
	 * @param position the position
	 */
	public NONCRDT_Data_Field(String dFN, String tN, String dT, boolean iPK, boolean iAIC, int position,
							  SemanticPolicy policy) {
		super(CrdtDataFieldType.NONCRDTFIELD, dFN, tN, dT, iPK, iAIC,
				position, policy);
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
	 */
	/**
	 * @see DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
	 * @param rs
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Crdt_Form(ResultSet rs, String Value) {
		try {
			throw new RuntimeException(
					"NONCRDT data should not be transformed!");
		} catch (RuntimeException e) {
			System.exit(ExitCode.INVALIDUSAGE);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see crdts.basics.Data_Field#get_Crdt_Form(java.lang.String)
	 */
	/**
	 * @see DataField#get_Crdt_Form(java.lang.String)
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Crdt_Form(String Value) {
		try {
			throw new RuntimeException(
					"NONCRDT data should not be transformed!");
		} catch (RuntimeException e) {
			System.exit(ExitCode.INVALIDUSAGE);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Data_Field#get_Value_In_Correct_Format(java.lang.String)
	 */
	/**
	 * @see DataField#get_Value_In_Correct_Format(java.lang.String)
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Value_In_Correct_Format(String Value) {
		return Value;
	}

}
