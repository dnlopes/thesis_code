package runtime;


import runtime.operation.ShadowOperation;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{

	public static ThriftOperation encodeThriftOperation(ShadowOperation shadowOperation)
	{
		//TODO
		ThriftOperation thriftOperation = new ThriftOperation();

		return  thriftOperation;
	}

	public static ShadowOperation decodeThriftOperation(ThriftOperation thriftOperation)
	{
		//TODO
		ShadowOperation shadowOperation = new ShadowOperation();
		return  shadowOperation;
	}

	
}
