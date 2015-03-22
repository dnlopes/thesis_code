package network.node;


import database.jdbc.ConnectionFactory;
import network.IReplicatorNetwork;
import network.ReplicatorNetwork;
import network.server.ReplicatorServerThread;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Configuration;
import runtime.TransactionId;
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
	private Map<String, AbstractNode> otherReplicators;
	//saves all txn already committed
	private Set<TransactionId> committed;
	
	public Replicator(String hostName, int id, int port)
	{
		super(hostName, id, port, Role.REPLICATOR);
		this.otherReplicators = new HashMap<>();
		this.committed = new HashSet<>();

		this.networkInterface = new ReplicatorNetwork(this);

		try
		{
			this.originalConn = ConnectionFactory.getDefaultConnection(Configuration.DB_NAME);
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
			LOG.error("failed to create background thread on replicator {}", this.getName());
			e.printStackTrace();
		}

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
		//TODO commit the operation in main database
		// should block until decision is made
		//1- execute locally
		//2- send async to others

		boolean commitDecision = this.executeShadowOperation(shadowOperation);

		for(AbstractNode node : otherReplicators.values())
			this.networkInterface.sendOperationAsync(shadowOperation, node);

		return commitDecision;
	}

	public boolean alreadyCommitted(TransactionId txnId)
	{
		return this.committed.contains(txnId);
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
		TransactionId txnId = new TransactionId(shadowOp.getTxnId());
		this.committed.add(txnId);

		LOG.info("txn {} committed", shadowOp.getTxnId());
		return true;
	}
}
