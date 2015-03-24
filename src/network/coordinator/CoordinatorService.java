package network.coordinator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CoordinatorRPC;
import util.thrift.ThriftCheckRequest;
import util.thrift.ThriftCheckResponse;

/**
 * Created by dnlopes on 24/03/15.
 */
public class CoordinatorService implements CoordinatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(CoordinatorService.class);

	private Coordinator coordinator;

	public CoordinatorService(Coordinator coordinator)
	{
		this.coordinator = coordinator;
	}

	@Override
	public ThriftCheckResponse checkInvariants(ThriftCheckRequest checkRequest) throws TException
	{
		LOG.trace("received new invariants list to verify");
		return null;
	}
}
