package network.node;


import database.util.DatabaseMetadata;
import network.server.CoordinatorServerThread;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import util.thrift.CheckInvariantThrift;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by dnlopes on 22/03/15.
 */
public class Coordinator extends AbstractNode
{
	static final Logger LOG = LoggerFactory.getLogger(Coordinator.class);


	private DatabaseMetadata databaseMetadata;
	private Map<String, Set<String>> uniques;
	private Map<String, Long> autoIncremented;
	private CoordinatorServerThread serverThread;


	public Coordinator(NodeMetadata nodeInfo)
	{
		super(nodeInfo);

		this.databaseMetadata = Configuration.getInstance().getDatabaseMetadata();
		this.uniques = new HashMap<>();
		this.autoIncremented = new HashMap<>();

		try
		{
			this.serverThread = new CoordinatorServerThread(this);
			new Thread(this.serverThread).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}", this.getName());
			e.printStackTrace();
		}
	}

	public void processInvariants(List<CheckInvariantThrift> checkList)
	{
		LOG.trace("processing invariants list");
	}





}
