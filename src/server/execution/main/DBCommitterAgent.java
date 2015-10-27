package server.execution.main;


import client.jdbc.ConnectionFactory;
import common.nodes.NodeConfig;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.CRDTCompiledTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 06/04/15.
 */
public class DBCommitterAgent implements DBCommitter
{

	private static final Logger LOG = LoggerFactory.getLogger(DBCommitterAgent.class);
	private static int TXN_COUNT = 0;

	private Connection connection;

	public DBCommitterAgent(NodeConfig config)
	{
		try
		{
			this.connection = ConnectionFactory.getDefaultConnection(config);
		} catch(SQLException e)
		{
			LOG.warn("failed to create connection for DBCommitPad", e);
		}
	}

	@Override
	public boolean commitShadowTransaction(CRDTCompiledTransaction txn)
	{
		TXN_COUNT++;

		if(TXN_COUNT % Defaults.LOG_FREQUENCY == 0)
			if(LOG.isInfoEnabled())
				LOG.info("txn {} from replicator {} committing on main storage ", txn.getId(), txn.getReplicatorId());

		if(LOG.isTraceEnabled())
			LOG.trace("commiting op from replicator {}", txn.getReplicatorId());

		for(int i = 0; i < Defaults.NUMBER_OF_RETRIES; i++)
		{
			boolean commitDecision = this.tryCommit(txn);

			if(commitDecision)
				return true;
		}

		return false;
	}

	private boolean tryCommit(CRDTCompiledTransaction op)
	{
		Statement stat = null;
		boolean success = false;

		try
		{
			stat = this.connection.createStatement();
			for(String statement : op.getOpsList())
			{
				if(LOG.isTraceEnabled())
					LOG.trace("executing on main storage: {}", statement);

				stat.addBatch(statement);
			}

			stat.executeBatch();
			this.connection.commit();
			success = true;

			if(LOG.isTraceEnabled())
				LOG.trace("txn {} committed", op.getId());

		} catch(SQLException e)
		{
			try
			{
				DbUtils.rollback(this.connection);
				LOG.warn("txn {} rollback ({})", op.getId(), e.getMessage());
			} catch(SQLException e1)
			{
				LOG.warn("failed to rollback txn {}", op.getId(), e1);
			}
		} finally
		{
			DbUtils.closeQuietly(stat);
		}

		return success;
	}
}
