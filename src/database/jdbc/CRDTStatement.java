package database.jdbc;


import database.jdbc.util.DBUpdateResult;
import database.occ.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import network.Proxy;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.Transaction;
import runtime.operation.DBSingleOperation;
import runtime.MyShadowOpCreator;
import runtime.operation.ShadowOperation;
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
	private ShadowOperation operation;

	public CRDTStatement(Proxy proxy, MyShadowOpCreator creator, Transaction transaction)
	{
		this.proxy = proxy;
		this.shdOpCreator = creator;
		this.transaction = transaction;
		this.operation = this.transaction.getShadowOp();
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		if(!this.transaction.hasBegun())
			this.proxy.beginTxn(transaction);

		ResultSet rs;
		try
		{
			DBSingleOperation dbOp = DBSingleOperation.createOperation(arg0);
			rs = proxy.executeQuery(dbOp, this.transaction.getTxnId());
			shdOpCreator.setCachedResultSetForDelta(rs);

		} catch(JSQLParserException | ScratchpadException e)
		{
			e.printStackTrace();
			LOG.error("failed to execute: {}", arg0);
			throw new SQLException(e);
		}

		if(this.transaction.isInternalAborted())
			throw new SQLException(this.transaction.getAbortMessage());

		LOG.trace("query statement executed properly");
		return rs;
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		if(!this.transaction.hasBegun())
			this.proxy.beginTxn(transaction);

		String[] deterStatements;
		try
		{
			deterStatements = shdOpCreator.makeToDeterministic(arg0);

		} catch(JSQLParserException e)
		{
			LOG.error("failed to generate deterministic statements: {}", arg0);
			throw new SQLException(e.getMessage());
		}

		int result = 0;

		for(String updateStr : deterStatements)
		{
			DBUpdateResult res;
			DBSingleOperation sqlOp;
			try
			{
				sqlOp = DBSingleOperation.createOperation(updateStr);
				Result tempRes = this.proxy.executeUpdate(sqlOp, this.transaction.getTxnId());
				res = DBUpdateResult.createResult(tempRes.getResult());
				result += res.getUpdateResult();

			} catch(JSQLParserException | ScratchpadException e)
			{
				e.printStackTrace();
				LOG.error("failed to execute: {}", arg0);
				throw new SQLException(e.getMessage());
			}

			if(this.operation == null)
			{
				ShadowOperation shdOp = new ShadowOperation(this.transaction);
				this.transaction.setShadowOp(shdOp);
				//shdOpCreator.createEmptyShadowOperation();
				this.operation = shdOp;
				//this.transaction.setShadowOp(shdOp);
				shdOpCreator.setShadowOperation(shdOp);
			}
			try
			{
				shdOpCreator.addDBEntryToShadowOperation(this.operation, updateStr, sqlOp);
			} catch(JSQLParserException e)
			{
				LOG.error("failed to add statement to shadow operation: {}", arg0);
				throw new SQLException(e.getMessage());
			}
		}

		if(this.transaction.isInternalAborted())
			throw new SQLException(this.transaction.getAbortMessage());

		DBUpdateResult finalRes = DBUpdateResult.createResult(result);

		LOG.trace("update statement executed properly");
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
