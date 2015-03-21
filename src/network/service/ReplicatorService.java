package network.service;


import network.node.Replicator;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;

import java.util.List;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{
	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);

	private Replicator replicator;

	public ReplicatorService(Replicator replicator)
	{
		this.replicator = replicator;
	}

	@Override
	public boolean commitOperation(ThriftOperation shadowOp) throws TException
	{
		List<String> operations = shadowOp.getOperations();
		LOG.info("op: {}", operations.get(0));
		return true;
	}

	@Override
	public boolean commitOperationSync(ThriftOperation shadowOp) throws TException
	{
		return false;
	}

	@Override
	public void commitOperationAsync(ThriftOperation shadowOp) throws TException
	{

	}
}
