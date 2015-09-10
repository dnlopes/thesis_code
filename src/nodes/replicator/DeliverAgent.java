package nodes.replicator;


import util.thrift.ThriftShadowTransaction;


/**
 * Created by dnlopes on 23/05/15.
 *
 * A deliver agent that dispatch operations to the replicator when appropriate.
 * When the replicator service receives remote operations from other nodes it tunnels them to this agent
 * The agent is then responsible to deliver the operations to the replicator entity when it is appropriate.
 * Currently there are two delivery policies implemented: causal delivery and 'no order' delivery
 */
public interface DeliverAgent
{
	public void dispatchOperation(ThriftShadowTransaction op);
}
