package database.execution.main;


import database.jdbc.ConnectionFactory;
import nodes.NodeConfig;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.DBDefaults;
import util.thrift.ThriftShadowTransaction;

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
	public boolean commitShadowTransaction(ThriftShadowTransaction op)
	{
		TXN_COUNT++;

		if(TXN_COUNT % Defaults.LOG_FREQUENCY == 0)
			if(LOG.isInfoEnabled())
				LOG.info("txn {} from replicator {} committing on main storage ", op.getTxnId(), op.getReplicatorId());

		if(LOG.isTraceEnabled())
			LOG.trace("commiting op from replicator {}", op.getReplicatorId());

		for(int i = 0; i < Defaults.NUMBER_OF_RETRIES; i++)
		{
			boolean commitDecision = this.tryCommit(op);

			if(commitDecision)
				return true;
		}

		return false;
	}

	private boolean tryCommit(ThriftShadowTransaction op)
	{
		Statement stat = null;
		boolean success = false;

		try
		{
			stat = this.connection.createStatement();
			for(String statement : op.getOperations().values())
			{
				String rebuiltStatement = this.replacePlaceholders(op, statement);
				if(LOG.isTraceEnabled())
					LOG.trace("executing on main storage: {}", rebuiltStatement);

				stat.addBatch(rebuiltStatement);
			}

			stat.executeBatch();
			this.connection.commit();
			success = true;

			if(LOG.isTraceEnabled())
				LOG.trace("txn {} committed", op.getTxnId());

		} catch(SQLException e)
		{
			try
			{
				DbUtils.rollback(this.connection);
				LOG.warn("txn {} rollback ({})", op.getTxnId(), e.getMessage());
			} catch(SQLException e1)
			{
				LOG.warn("failed to rollback txn {}", op.getTxnId(), e1);
			}
		} finally
		{
			DbUtils.closeQuietly(stat);
		}

		return success;
	}

	private String replacePlaceholders(ThriftShadowTransaction op, String statement)
	{
		String clockString = op.getClock();
		statement = statement.replaceAll(DBDefaults.CLOCK_VALUE_PLACEHOLDER, clockString);

		return statement;
	}
}
