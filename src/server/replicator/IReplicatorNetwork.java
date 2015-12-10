package server.replicator;

import common.thrift.CRDTCompiledTransaction;
import common.thrift.CRDTPreCompiledTransaction;
import common.thrift.CoordinatorRequest;
import common.thrift.CoordinatorResponse;
import common.util.exception.SocketConnectionException;

import java.util.List;


/**
 * @author dnlopes
 *         This interface defines methods for all Replicator communications.
 *         All methods should be thread-safe, because this class is used by multiple threads to send events to
 *         remote nodes
 */
public interface IReplicatorNetwork
{
	void sendOperationToRemote(CRDTCompiledTransaction transaction);
	void sendBatchToRemote(List<CRDTCompiledTransaction> transactions);

	CoordinatorResponse sendRequestToCoordinator(CoordinatorRequest req) throws SocketConnectionException;
}
