package runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import util.thrift.ThriftOperation;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);


	public static ThriftOperation encodeThriftOperation(ShadowOperation shadowOperation)
	{
		//TODO
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
