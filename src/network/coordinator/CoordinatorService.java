package network.coordinator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CoordRequestMessage;
import util.thrift.CoordResponseMessage;
import util.thrift.CoordinatorRPC;
import util.thrift.ResponseEntry;

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
	public CoordResponseMessage checkInvariants(CoordRequestMessage checkRequest) throws TException
	{
		LOG.trace("request {} received", checkRequest.getMessageId());
		CoordResponseMessage newResponse = new CoordResponseMessage();
		newResponse.setMessageId(checkRequest.getMessageId());

		List<ResponseEntry> checkResultList = this.coordinator.processInvariants(checkRequest.getRequests());

		// something went wrong. set boolean false and send response
		if(checkResultList == null)
		{
			newResponse.setSuccess(false);
			LOG.error("txn is not allowed to commit. Please abort");
			return newResponse;
		}

		newResponse.setSuccess(true);
		newResponse.setResponses(checkResultList);
		LOG.info("txn is allowed to commit.");
		return newResponse;
	}
}
