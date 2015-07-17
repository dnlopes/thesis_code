package util.thrift;


import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;


/**
 * Created by dnlopes on 16/07/15.
 */
public class ThriftUtils
{

	public static CoordinatorRequest decodeCoordinatorRequest(byte[] requestByteArray)
	{
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		CoordinatorRequest req = new CoordinatorRequest();
		try
		{
			deserializer.deserialize(req, requestByteArray);
			return req;
		} catch(TException e)
		{
			return null;
		}
	}

	public static byte[] encodeThriftObject(TBase request)
	{
		TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
		try
		{
			byte[] bytes = serializer.serialize(request);
			return bytes;
		} catch(TException e)
		{
			return null;
		}
	}

}
