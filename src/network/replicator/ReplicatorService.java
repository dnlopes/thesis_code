package network.replicator;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Utils;
import runtime.operation.ShadowOperation;
import util.thrift.ReplicatorRPC;
import util.thrift.ThriftOperation;


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
	public boolean commitOperation(ThriftOperation thriftOp)
	{
		LOG.trace("new ShadowOperation received");
		//System.out.println("RECEIVED");
		//here we decide how a operation commit happens.
		//for now, we commit locally, send to othe replicators but dont wait for their responses
		ShadowOperation shadowOp = Utils.decodeThriftOperation(thriftOp);
		return this.replicator.commitOperation(shadowOp);
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
