package network.service;


import network.node.Coordinator;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.CheckInvariantThrift;
import util.thrift.CoordinatorRPC;

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
	public List<CheckInvariantThrift> checkInvariants(List<CheckInvariantThrift> checkList) throws TException
	{
		LOG.trace("received new invariants list to verify");
		this.coordinator.processInvariants(checkList);
		return checkList;
	}
}
