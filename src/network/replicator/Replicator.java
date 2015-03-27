package network.replicator;


import database.jdbc.ConnectionFactory;
import network.AbstractNode;
import network.AbstractConfig;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.Configuration;
import runtime.operation.ShadowOperation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by dnlopes on 15/03/15.
 */
public class Replicator extends AbstractNode
{
	
	static final Logger LOG = LoggerFactory.getLogger(Replicator.class);
	
	private IReplicatorNetwork networkInterface;
	private ReplicatorServerThread serverThread;
	private Connection originalConn;
	private Map<String, ReplicatorConfig> otherReplicators;
	//saves all txn already committed
	private Set<Long> committedTxns;
	
	public Replicator(ReplicatorConfig config)
	{
		super(config);

		this.otherReplicators = new HashMap<>();
		this.networkInterface = new ReplicatorNetwork(this.getConfig());

		for(ReplicatorConfig allReplicators : Configuration.getInstance().getReplicators().values())
			this.otherReplicators.put(allReplicators.getName(), allReplicators);

		this.committedTxns = new HashSet<>();

		try
		{
			this.originalConn = ConnectionFactory.getDefaultConnection(this.getConfig());
		} catch(SQLException e)
		{
			e.printStackTrace();
		}

		try
		{
			this.serverThread = new ReplicatorServerThread(this);
			new Thread(this.serverThread).start();
		} catch(TTransportException e)
		{
			LOG.error("failed to create background thread on replicator {}", this.getConfig().getName());
			e.printStackTrace();
		}

		LOG.info("replicator {} online", this.config.getId());
	}

	@Override
	public ReplicatorConfig getConfig()
	{
		return (ReplicatorConfig) this.config;
	}

	/**
	 * Attempts to commit a shadow operation.
	 * First it executes locally, and then it is async propagated to other replicators.
	 *
	 * @param shadowOperation
	 *
	 * @return true if it was sucessfull committed locally, false otherwise
	 */
	public boolean commitOperation(ShadowOperation shadowOperation)
	{
		if(this.alreadyCommitted(shadowOperation.getTxnId()))
		{
			LOG.warn("duplicated transaction {}. Ignored.", shadowOperation.getTxnId());
			return true;
		}
		/*	should block until decision is made
			1- execute locally
			2- send async to others
		*/

		boolean commitDecision = this.executeShadowOperation(shadowOperation);

		for(AbstractConfig node : otherReplicators.values())
			this.networkInterface.sendOperationAsync(shadowOperation, node);

		return commitDecision;
	}

	public boolean alreadyCommitted(Long txnId)
	{
		return this.committedTxns.contains(txnId);
	}

	private boolean executeShadowOperation(ShadowOperation shadowOp)
	{
		Statement stat;
		try
		{
			stat = this.originalConn.createStatement();
			for(String statement : shadowOp.getOperationList())
			{
				LOG.trace("executing on maindb: {}", statement);
				stat.execute(statement);
			}

			this.originalConn.commit();

		} catch(SQLException e)
		{
			LOG.error("failed to execute operation {} in main database", shadowOp.getTxnId());
			e.printStackTrace();
			return false;
		}
		this.committedTxns.add(shadowOp.getTxnId());

		LOG.info("txn {} committed", shadowOp.getTxnId());
		return true;
	}
}
