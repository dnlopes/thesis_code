package runtime;

import database.util.DataField;
import database.util.DatabaseFunction;
import runtime.operation.ShadowTransaction;
import util.thrift.ThriftOperation;

import java.text.DateFormat;
import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public class RuntimeUtils
{

	public static ThriftOperation encodeThriftOperation(ShadowTransaction shadowTransaction)
	{
		ThriftOperation thriftOperation = new ThriftOperation();
		thriftOperation.setOperations(shadowTransaction.getOperationList());
		thriftOperation.setTxnId(shadowTransaction.getTxnId());

		return thriftOperation;
	}

	public static ShadowTransaction decodeThriftOperation(ThriftOperation thriftOperation)
	{
		List<String> ops = thriftOperation.getOperations();
		return new ShadowTransaction(thriftOperation.getTxnId(), ops);
	}

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
			return "'" + DatabaseFunction.CURRENTTIMESTAMP(dateFormat) + "'";
		default:
			System.err.println("cannot get default value for primitive type" + df.toString());
			throw new RuntimeException("not such crdt type");
		}
	}
}
