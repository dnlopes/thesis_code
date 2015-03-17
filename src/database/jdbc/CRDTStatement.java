package database.jdbc;

import database.jdbc.util.DBUpdateResult;
import database.occ.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import network.Proxy;
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

	public CRDTStatement(Proxy proxy, MyShadowOpCreator creator, Transaction transaction)
	{
		this.proxy = proxy;
		this.shdOpCreator = creator;
		this.transaction = transaction;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		//TODO review
		if(!this.transaction.hasBegun())
			this.proxy.beginTxn(transaction);

		//ResultSet result = this.proxy.executeQuery(arg0);

		DBSelectResult res;
		CRDTResultSet resultSet;

		try
		{
			Result r = proxy.execute(DBSingleOperation.createOperation(arg0), this.transaction.getTxnId());
			res = DBSelectResult.createResult(r);
			resultSet = new CRDTResultSet(res);

			shdOpCreator.setCachedResultSetForDelta(resultSet);

			LOG.trace("query statement executed properly");
			return resultSet;

		} catch(JSQLParserException | ScratchpadException e)
		{
			e.printStackTrace();
			System.out.println(arg0);
			throw new SQLException(e);
		}
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		//TODO review
		if(!this.transaction.hasBegun())
			this.proxy.beginTxn(transaction);

		String[] deterStatements;
		try
		{
			deterStatements = shdOpCreator.makeToDeterministic(arg0);
		} catch(JSQLParserException e)
		{
			LOG.error("failed to generate deterministic statements for txn {}", this.transaction.getTxnId().getId());
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
				Result tempRes = this.proxy.execute(sqlOp, this.transaction.getTxnId());
				res = DBUpdateResult.createResult(tempRes.getResult());
				result += res.getUpdateResult();

			} catch(JSQLParserException | ScratchpadException e)
			{
				LOG.error("failed to execute statement in scratchpad state for txn {}",
						this.transaction.getTxnId().getId());
				throw new SQLException(e.getMessage());
			}

			if(this.transaction.getShadowOp() == null)
			{
				ShadowOperation shdOp = shdOpCreator.createEmptyShadowOperation();
				this.transaction.setShadowOp(shdOp);
				shdOpCreator.setShadowOperation(shdOp);
			}
			try
			{
				shdOpCreator.addDBEntryToShadowOperation(this.transaction.getShadowOp(), updateStr, sqlOp);
			} catch(JSQLParserException e)
			{
				LOG.error("failed to add statement to shadow operation for txn {}",
						this.transaction.getTxnId().getId());
				throw new SQLException(e.getMessage());
			}
		}
		LOG.trace("update statement executed properly");
		return result;
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
