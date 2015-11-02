package server.execution.main;


import common.nodes.NodeConfig;
import common.util.ConnectionFactory;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.thrift.CRDTCompiledTransaction;
import server.execution.StatsCollector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 06/04/15.
 */
public class DBCommitterAgent implements DBCommitter
{

	private static final Logger LOG = LoggerFactory.getLogger(DBCommitterAgent.class);

	private Connection connection;
	private StatsCollector collector;

	public DBCommitterAgent(NodeConfig config, StatsCollector collector)
	{
		this.collector = collector;

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
		int tries = 0;

		while(true)
		{
			tries++;
			boolean commitDecision = this.tryCommit(txn);

			if(commitDecision)
			{
				this.collector.incrementCommits();
				return true;
			}
			else
			{
				this.collector.incrementRetries();

				if(tries % Defaults.LOG_FREQUENCY == 0)
					LOG.warn("already tried {} times but still no commit", tries);
			}
		}
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
