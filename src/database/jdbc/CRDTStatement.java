package database.jdbc;


import database.jdbc.util.DBUpdateResult;
import database.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import network.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.txn.Transaction;
import runtime.operation.DBSingleOperation;
import runtime.MyShadowOpCreator;
import util.MissingImplementationException;

import java.sql.*;


/**
 * Created by dnlopes on 04/03/15.
 */
public class CRDTStatement implements Statement
{

	static final Logger LOG = LoggerFactory.getLogger(CRDTStatement.class);

	private MyShadowOpCreator shdOpCreator;
	private Transaction transaction;
	private Proxy proxy;

	public CRDTStatement(Proxy proxy, MyShadowOpCreator creator, Transaction transaction)
	{
		this.proxy = proxy;
		this.shdOpCreator = creator;
		this.transaction = transaction;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		if(!this.transaction.hasBegun())
			this.proxy.beginTransaction(transaction);

		ResultSet rs;
		try
		{
			DBSingleOperation dbOp = new DBSingleOperation(arg0);
			rs = proxy.executeQuery(dbOp, this.transaction.getTxnId());
			shdOpCreator.setCachedResultSetForDelta(rs);

		} catch(JSQLParserException | ScratchpadException e)
		{
			LOG.error("failed to execute: {}", arg0, e);
			throw new SQLException(e);
		}

		LOG.trace("query statement executed properly");
		return rs;
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		if(!this.transaction.hasBegun())
			this.proxy.beginTransaction(transaction);

		String[] deterStatements;
		try
		{
			deterStatements = shdOpCreator.makeToDeterministic(arg0);

		} catch(JSQLParserException e)
		{
			LOG.error("failed to generate deterministic statements: {}", arg0, e);
			throw new SQLException(e);
		}

		int result = 0;

		for(String updateStr : deterStatements)
		{
			DBUpdateResult res;
			DBSingleOperation dbOp;
			try
			{
				dbOp = new DBSingleOperation(updateStr);
				Result tempRes = this.proxy.executeUpdate(dbOp, this.transaction.getTxnId());
				res = DBUpdateResult.createResult(tempRes.getResult());
				result += res.getUpdateResult();

			} catch(JSQLParserException | ScratchpadException e)
			{
				LOG.error("failed to execute: {}", arg0, e);
				throw new SQLException(e);
			}
		}

		DBUpdateResult finalRes = DBUpdateResult.createResult(result);

		LOG.trace("update statement executed properly");

		this.transaction.setNotReadOnly();
		return finalRes.getUpdateResult();
	}

/*
	NOT IMPLEMENTED METHODS START HERE
*/

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void addBatch(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void cancel() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearBatch() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void close() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, int arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int[] executeBatch() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getFetchDirection() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getFetchSize() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getMaxFieldSize() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getMaxRows() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean getMoreResults() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean getMoreResults(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getQueryTimeout() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ResultSet getResultSet() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetConcurrency() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetHoldability() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getResultSetType() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getUpdateCount() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setCursorName(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setMaxFieldSize(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setMaxRows(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setPoolable(boolean arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setQueryTimeout(int arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public void closeOnCompletion() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public boolean isCloseOnCompletion() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}
}
