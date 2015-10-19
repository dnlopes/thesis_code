package nodes.replicator.deliver;


import util.thrift.CRDTCompiledTransaction;


/**
 * Created by dnlopes on 23/05/15.
 * <p/>
 * A deliver agent propagates transactions to the local replicator when appropriate.
 * When the replicator service receives remote transactions from other replicators, it tunnels them to this agent
 * This agent is then responsible to deliver the operations to the local replicator when it is appropriate.
 * Currently there are two delivery policies implemented: causal delivery and 'no order' delivery
 */
public interface DeliverAgent
{

	void deliverTransaction(CRDTCompiledTransaction op);
}
