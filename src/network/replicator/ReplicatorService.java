package network.replicator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.ExitCode;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;


/**
 * Created by dnlopes on 20/03/15.
 */
public class ReplicatorService implements ReplicatorRPC.Iface
{

	private static final Logger LOG = LoggerFactory.getLogger(ReplicatorService.class);

	private Replicator replicator;
	private IReplicatorNetwork network;

	public ReplicatorService(Replicator replicator, IReplicatorNetwork network)
	{
		this.replicator = replicator;
		this.network = network;
	}

	@Override
	public boolean commitOperation(ThriftOperation thriftOp)
	{
		LOG.trace("new ShadowOperation received");
		ShadowOperation decodedOp = Utils.decodeThriftOperation(thriftOp);
		boolean localCommit;

		// just deliver the operation to own replicator and wait for commit decision.
		// if it suceeds then deliver the operation to other replicators

		localCommit = this.replicator.commitOperation(decodedOp);
		if(localCommit)
			network.sendOperationAsync(thriftOp);

		return localCommit;
	}

	@Override
	public boolean commitOperationSync(ThriftOperation shadowOp) throws TException
	{
		RuntimeHelper.throwRunTimeException("missing implementation", ExitCode.MISSING_IMPLEMENTATION);
		return false;
	}

	@Override
	public void commitOperationAsync(ThriftOperation shadowOp) throws TException
	{
		LOG.debug("received op from other replicator");
		ShadowOperation decodedOp = Utils.decodeThriftOperation(shadowOp);
		this.replicator.commitOperation(decodedOp);
	}
}
