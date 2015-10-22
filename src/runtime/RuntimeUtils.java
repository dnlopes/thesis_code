package runtime;


import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import util.thrift.CoordinatorResponse;


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

	public static CoordinatorResponse decodeCoordinatorResponse(byte[] bytesObject)
	{
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		CoordinatorResponse req = new CoordinatorResponse();
		try
		{
			deserializer.deserialize(req, bytesObject);
			return req;
		} catch(TException e)
		{
			return null;
		}
	}

}
