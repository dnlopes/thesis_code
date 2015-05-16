package database.scratchpad;


import database.jdbc.ConnectionFactory;
import network.AbstractNodeConfig;
import network.replicator.Replicator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.ShadowOperation;
import util.defaults.DBDefaults;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 06/04/15.
 */
public class DBCommitPad implements IDBCommitPad
{

	private static final Logger LOG = LoggerFactory.getLogger(DBCommitPad.class);
	private static final int NUMBER_OF_RETRIES = 10;
	private StopWatch watcher;
	private Replicator replicator;
	private AbstractNodeConfig config;

	private Connection connection;

	public DBCommitPad(Replicator replicator)
	{
		this.replicator = replicator;
		this.config = this.replicator.getConfig();
		this.watcher = new StopWatch();

		try
		{
			this.connection = ConnectionFactory.getDefaultConnection(config);
		} catch(SQLException e)
		{
			LOG.error("failed to create connection for DBCommitPad", e);
		}
	}

	@Override
	public boolean commitShadowOperation(ShadowOperation op)
	{
		this.resetCommitPad();

		this.watcher.start();
		for(int i = 0; i < NUMBER_OF_RETRIES; i++)
		{
			boolean commitDecision = this.tryCommit(op);

			if(commitDecision)
			{
				this.watcher.stop();
				return true;
			}
		}
		LOG.error("failed to commit after {} retries", NUMBER_OF_RETRIES);
		return false;
	}

	private boolean tryCommit(ShadowOperation op)
	{
		Statement stat = null;
		boolean success = false;
		try
		{
			stat = this.connection.createStatement();
			for(String statement : op.getOperationList())
			{
				String rebuiltStatement = this.replacePlaceholders(op, statement);
				LOG.info("executing on maindb: {}", rebuiltStatement);

				stat.addBatch(rebuiltStatement);
			}
			stat.executeBatch();
			this.connection.commit();
			success = true;

			LOG.info("txn {} committed", op.getTxnId());

			DbUtils.closeQuietly(stat);
		} catch(SQLException e)
		{
			DbUtils.closeQuietly(stat);
			try
			{
				DbUtils.rollback(this.connection);
			} catch(SQLException e1)
			{
				// this should not happen
				LOG.error("failed to rollback txn {} (should not happen)", op.getTxnId(), e1);
			}
			LOG.error("txn {} rollback ({})", op.getTxnId(), e.getMessage());
		}
		return success;
	}

	private void resetCommitPad()
	{
		this.watcher.reset();
	}

	public long getCommitLatency()
	{
		return this.watcher.getTime();
	}

	private String replacePlaceholders(ShadowOperation op, String statement)
	{
		String clockString = op.getClock().getClockValue();
		statement = statement.replaceFirst(DBDefaults.CONTENT_CLOCK_PLACEHOLDER, clockString);
		statement = statement.replaceFirst(DBDefaults.DELETED_CLOCK_PLACEHOLDER, clockString);
		return statement;
	}
}
