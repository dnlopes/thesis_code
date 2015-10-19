package nodes.replicator.dispatcher;


import util.thrift.CRDTTransaction;


/**
 * Created by dnlopes on 08/10/15.
 * <p/>
 * A dispatcher agent is responsible for propagating transactions to remote replicators when
 * appropriate.
 * When the local replicator sucessfully commits a transaction, it dispatch the transaction to this agent.
 * This agent is responsible for sending the transaction to remote replicators when it is appropriate.
 * Currently there are two dispatch policies implemented: basic dispatcher and aggregator dispatcher
 * <p/>
 * Basic dispatcher immediately forwards incoming transactions to remote replicators.
 * Aggregator dispatcher caches incoming transactions and merges some of them when possible (for instance 2
 * consecutives updates to the same @LWW field). Periodically, transactions are sent as a batch to remote replicators
 */
public interface DispatcherAgent
{
	void dispatchTransaction(CRDTTransaction op);
}
