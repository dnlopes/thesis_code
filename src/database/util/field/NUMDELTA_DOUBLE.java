package database.util.field;


import java.sql.ResultSet;
import java.sql.SQLException;

import database.util.SemanticPolicy;
import util.ExitCode;

// TODO: Auto-generated Javadoc


/**
 * The Class NUMDELTA_DOUBLE.
 */
public class NUMDELTA_DOUBLE extends DataField
{

	/**
	 * Instantiates a new numdelta double.
	 *
	 * @param dFN
	 * 		the d fn
	 * @param tN
	 * 		the t n
	 * @param dT
	 * 		the d t
	 * @param iPK
	 * 		the i pk
	 * @param iFK
	 * 		the i fk
	 * @param iAIC
	 * 		the i aic
	 * @param position
	 * 		the position
	 */
	public NUMDELTA_DOUBLE(String dFN, String tN, String dT, boolean iPK, boolean iAIC, int position, SemanticPolicy policy)
	{
		super(CrdtDataFieldType.NUMDELTADOUBLE, dFN, tN, dT, iPK, iAIC, position, policy);
	}

	/**
	 * Apply_ delta.
	 *
	 * @param delta
	 * 		the delta
	 *
	 * @return the string
	 */
	public String apply_Delta(double delta)
	{
		String transformedSql = "";
		if(delta >= 0)
			transformedSql = this.getFieldName() + " = " + this.getFieldName() + "+" + delta;
		else
			transformedSql = this.getFieldName() + " = " + this.getFieldName() + delta;
		return transformedSql;
	}

	@Override
	public boolean isDeltaField()
	{
		return true;
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

		if(rs == null)
		{
			try
			{
				throw new RuntimeException("ResultSet is null!");
			} catch(RuntimeException e)
			{
				System.exit(ExitCode.NULLPOINTER);
			}
		}

		try
		{
			rs.next();
			double originalValue = rs.getDouble(this.getFieldName());
			double finalValue = Double.parseDouble(Value);
			double delta = finalValue - originalValue;
			rs.beforeFirst();
			return apply_Delta(delta);
		} catch(SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(ExitCode.SQLRESULTSETNOTFOUND);
		}

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
		Value = Value.trim();
		return apply_Delta(Double.parseDouble(Value));
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

}
