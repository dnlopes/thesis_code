package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import util.thrift.ThriftOperation;



/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);


	public static ThriftOperation encodeThriftOperation(ShadowOperation shadowOperation)
	{
		//TODO
		return new ThriftOperation(shadowOperation.getTxnId(), shadowOperation.getOperationList());
	}

	public static ShadowOperation decodeThriftOperation(ThriftOperation thriftOperation)
	{
		//TODO
		return new ShadowOperation(thriftOperation.getTxnId(), thriftOperation
				.getOperations());
	}


}
