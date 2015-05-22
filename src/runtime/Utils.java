package runtime;

import runtime.operation.ShadowOperation;
import util.thrift.ThriftOperation;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{

	public static ThriftOperation encodeThriftOperation(ShadowOperation shadowOperation)
	{
		ThriftOperation thriftOperation = new ThriftOperation();
		thriftOperation.setOperations(shadowOperation.getOperationList());
		thriftOperation.setTxnId(shadowOperation.getTxnId());

		return thriftOperation;
	}

	public static ShadowOperation decodeThriftOperation(ThriftOperation thriftOperation)
	{
		List<String> ops = thriftOperation.getOperations();
		return new ShadowOperation(thriftOperation.getTxnId(), ops);
	}

}
