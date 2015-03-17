package network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.Operation;

/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends Node
{

	static final Logger LOG = LoggerFactory.getLogger(Replicator.class);


	public Replicator(String hostName, int id, int port)
	{
		super(hostName, id, port, Role.REPLICATOR);
	}

	public boolean commit(Operation op)
	{
		boolean success = false;

		LOG.trace("received commit signal");
		//TODO
		// 1- commit locally (we need a connection instance)
		// 2- send to other replicas in a background thread?
		return success;
	}
}
