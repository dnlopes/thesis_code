package runtime;


import database.util.field.DataField;
import database.util.DatabaseCommon;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import util.thrift.CoordinatorRequest;
import util.thrift.CoordinatorResponse;

import java.text.DateFormat;



/**
 * Created by dnlopes on 21/03/15.
 */
public class RuntimeUtils
{

	private static final TSerializer T_SERIALIZER = new TSerializer(new TBinaryProtocol.Factory());
	private static final TDeserializer T_DESERIALIZER = new TDeserializer(new TBinaryProtocol.Factory());

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

	public static byte[] encodeThriftObject(TBase request)
	{
		try
		{
			byte[] bytes = T_SERIALIZER.serialize(request);
			return bytes;
		} catch(TException e)
		{
			return null;
		}
	}

	public static CoordinatorResponse decodeCoordinatorResponse(byte[] bytesObject)
	{
		CoordinatorResponse req = new CoordinatorResponse();
		try
		{
			T_DESERIALIZER.deserialize(req, bytesObject);
			return req;
		} catch(TException e)
		{
			return null;
		}
	}

	public static CoordinatorRequest decodeCoordinatorRequest(byte[] requestByteArray)
	{
		CoordinatorRequest req = new CoordinatorRequest();
		try
		{
			T_DESERIALIZER.deserialize(req, requestByteArray);
			return req;
		} catch(TException e)
		{
			return null;
		}
	}

}
