package server.execution.main;


import common.nodes.NodeConfig;
import common.thrift.CRDTPreCompiledOperation;
import common.thrift.CRDTPreCompiledTransaction;
import common.thrift.Status;
import common.util.ConnectionFactory;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.TransactionCommitFailureException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 06/04/15.
 */
public class DBCommitterAgent implements DBCommitter
{

	private static final Logger LOG = LoggerFactory.getLogger(DBCommitterAgent.class);
	private static final Status SUCCESS_STATUS = new Status(true, null);

	private Connection connection;
	private String lastError;

	public DBCommitterAgent(NodeConfig config) throws SQLException
	{
		this.connection = ConnectionFactory.getDefaultConnection(config);
	}

	@Override
	public Status commitCrdtOperation(CRDTPreCompiledTransaction txn) throws TransactionCommitFailureException
	{
		int tries = 0;

		while(true)
		{
			tries++;
			if(tries == 50)
				throw new TransactionCommitFailureException("transaction failed to commit after 50 attempts");

			boolean commitDecision = tryCommit(txn);

			if(commitDecision)
				return SUCCESS_STATUS;
			else
			{
				LOG.warn(lastError);
				if(tries % Defaults.LOG_FREQUENCY == 0)
					LOG.warn("already tried {} times but still no commit", tries);
			}
		}
	}

	private boolean tryCommit(CRDTPreCompiledTransaction op)
	{
		Statement stat = null;
		boolean success = false;

		try
		{
			stat = this.connection.createStatement();
			for(CRDTPreCompiledOperation sqlOp : op.getOpsList())
				stat.addBatch(sqlOp.getSqlOp());

			stat.executeBatch();
			this.connection.commit();
			success = true;

			if(LOG.isTraceEnabled())
				LOG.trace("txn {} committed", op.getId());

		} catch(SQLException e)
		{
			try
			{
				lastError = e.getMessage();
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
