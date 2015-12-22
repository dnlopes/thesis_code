package weaql.common.database.field.lww;


import weaql.common.database.field.CRDTFieldType;
import weaql.common.database.util.SemanticPolicy;


public class LwwInteger extends LwwField
{

	public LwwInteger(String dFN, String tN, String dT, boolean iPK, boolean iAIC, int position, SemanticPolicy policy)
	{
		super(CRDTFieldType.LWWINTEGER, dFN, tN, dT, iPK, iAIC, position, policy);

	}

	@Override
	public String formatValue(String Value)
	{
		return Value;
	}

	@Override
	public boolean isNumberField()
	{
		return true;
	}

}
