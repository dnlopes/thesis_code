package runtime;

import runtime.operation.ShadowOperation;
import util.defaults.Configuration;
import util.thrift.ThriftOperation;

import java.util.List;


/**
 * Created by dnlopes on 21/03/15.
 */
public class Utils
{

	private static LogicalClock CURRENT_CLOCK = new LogicalClock(Configuration.getInstance().getAllReplicatorsConfig
			().size());

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

	public synchronized static LogicalClock getNextClock(int index)
	{
		LogicalClock newClock = new LogicalClock(CURRENT_CLOCK.getGeneration(), CURRENT_CLOCK.getDcEntries());
		newClock.increment(index);
		return newClock;
	}

}
