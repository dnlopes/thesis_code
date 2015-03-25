package network.coordinator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CoordinatorRPC;
import util.thrift.ThriftCheckRequest;
import util.thrift.ThriftCheckResponse;
import util.thrift.ThriftCheckResult;

import java.util.List;


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
		LOG.trace("request {} received");
		ThriftCheckResponse newResponse = new ThriftCheckResponse();
		newResponse.setResponseId(checkRequest.getRequestId());

		List<ThriftCheckResult> checkResultList = this.coordinator.processInvariants(checkRequest.getRequest());

		// something went wrong. set boolean false and send response
		if(checkResultList == null)
		{
			newResponse.setSuccess(false);
			LOG.error("txn is not allowed to commit. Please abort");
			return newResponse;
		}

		newResponse.setSuccess(true);
		newResponse.setResult(checkResultList);
		LOG.info("txn is allowed to commit.");
		return newResponse;
	}
}
