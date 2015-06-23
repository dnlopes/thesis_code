package runtime;


import database.util.field.DataField;
import database.util.DatabaseCommon;
import runtime.operation.ShadowOperation;
import util.thrift.ThriftShadowTransaction;

import java.text.DateFormat;
import java.util.HashMap;


/**
 * Created by dnlopes on 21/03/15.
 */
public class RuntimeUtils
{

	public static void throwRunTimeException(String message, int exitCode)
	{
		try
		{
			throw new RuntimeException(message);
		} catch(RuntimeException e)
		{
			e.printStackTrace();
			System.exit(exitCode);
		}
	}

	/**
	 * Gets the default value for data field.
	 *
	 * @param df
	 * 		the df
	 *
	 * @return the default value for data field
	 */
	public static String getDefaultValueForDataField(DateFormat dateFormat, DataField df)
	{
		switch(df.getCrdtType())
		{
		case NONCRDTFIELD:
			throw new RuntimeException("NONCRDT is depreciated");
		case NORMALINTEGER:
		case LWWINTEGER:
		case NUMDELTAINTEGER:
			return "0";
		case NORMALBOOLEAN:
		case LWWBOOLEAN:
			return "true";
		case NORMALFLOAT:
		case LWWFLOAT:
		case NUMDELTAFLOAT:
			return "0.0";
		case NORMALDOUBLE:
		case LWWDOUBLE:
		case NUMDELTADOUBLE:
			return "0.0";
		case NORMALSTRING:
		case LWWSTRING:
			return "'abc'";
		case NORMALDATETIME:
		case LWWDATETIME:
		case NUMDELTADATETIME:
			return "'" + DatabaseCommon.CURRENTTIMESTAMP(dateFormat) + "'";
		default:
			System.err.println("cannot get default value for primitive type" + df.toString());
			throw new RuntimeException("not such crdt type");
		}
	}

	public static ThriftShadowTransaction encodeShadowTransaction(Transaction txn)
	{
		ThriftShadowTransaction thriftTxn = new ThriftShadowTransaction();
		thriftTxn.setOperations(new HashMap<Integer, String>());
		thriftTxn.setTempOperations(new HashMap<Integer, String>());
		thriftTxn.setTxnId(txn.getTxnId());

		for(ShadowOperation op : txn.getShadowOperations())
			op.generateStatements(thriftTxn);

		return thriftTxn;
	}
}
