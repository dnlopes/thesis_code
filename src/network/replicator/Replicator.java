package network.replicator;


import database.jdbc.ConnectionFactory;
import network.AbstractNode;
import network.AbstractNodeConfig;
import org.apache.commons.dbutils.DbUtils;
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
	private Set<Integer> committedTxns;
	
	public Replicator(AbstractNodeConfig config)
	{
		super(config);

		this.otherReplicators = new HashMap<>();
		this.networkInterface = new ReplicatorNetwork(this.getConfig());

		for(ReplicatorConfig allReplicators : Configuration.getInstance().getAllReplicatorsConfig().values())
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

	/**
	 * Attempts to commit a shadow operation.
	 *
	 * @param shadowOperation
	 *
	 * @return true if it was sucessfully committed locally, false otherwise
	 */
	public boolean commitOperation(ShadowOperation shadowOperation)
	{
		LOG.trace("committing op");
		if(this.alreadyCommitted(shadowOperation.getTxnId()))
		{
			LOG.warn("duplicated transaction {}. Silently ignored.", shadowOperation.getTxnId());
			return true;
		}
		return this.executeShadowOperation(shadowOperation);
	}

	private boolean alreadyCommitted(int txnId)
	{
		return this.committedTxns.contains(txnId);
	}

	private boolean executeShadowOperation(ShadowOperation shadowOp)
	{
		Statement stat = null;
		boolean success = false;
		try
		{
			stat = this.originalConn.createStatement();
			for(String statement : shadowOp.getOperationList())
			{
				LOG.debug("executing on maindb: {}", statement);
				stat.addBatch(statement);
			}
			stat.executeBatch();
			this.originalConn.commit();
			success = true;
			this.committedTxns.add(shadowOp.getTxnId());
			LOG.info("txn {} committed", shadowOp.getTxnId());

			DbUtils.closeQuietly(stat);
		} catch(SQLException e)
		{
			DbUtils.closeQuietly(stat);
			try
			{
				DbUtils.rollback(this.originalConn);
			} catch(SQLException e1)
			{
				// this should not happen
				LOG.error("failed to rollback txn {}", shadowOp.getTxnId(), e1);
			}
			LOG.error("txn {} rollback ({})", shadowOp.getTxnId(), e.getMessage());
		}
		return success;
	}

	public IReplicatorNetwork getNetworkInterface()
	{
		return this.networkInterface;
	}
}
