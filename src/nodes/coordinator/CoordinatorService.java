package nodes.coordinator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.*;

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
	public CoordinatorResponse checkInvariants(CoordinatorRequest request) throws TException
	{
		LOG.trace("request {} received", request.getRequestId());

		CoordinatorResponse response = this.coordinator.processInvariants(request);

		if(!response.isSuccess())
			LOG.warn("txn is not allowed to commit. Please abort");
		else
			LOG.info("txn is allowed to commit.");

		return response;
	}
}
