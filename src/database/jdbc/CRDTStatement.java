package database.jdbc;


import nodes.proxy.Proxy;
import util.exception.MissingImplementationException;

import java.sql.*;


/**
 * Created by dnlopes on 04/03/15.
 */
public class CRDTStatement implements Statement
{

	private final int id;
	private final Proxy proxy;

	public CRDTStatement(int connectionId, Proxy proxy)
	{
		this.id = connectionId;
		this.proxy = proxy;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		return proxy.executeQuery(arg0, this.id);
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		return proxy.executeUpdate(arg0, this.id);
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
		this.proxy.closeTransaction(this.id);
	}

	@Override
	public boolean execute(String arg0) throws SQLException
	{
		if(arg0.contains("commit"))
		{
			if(!proxy.commit(this.id))
				throw new SQLException("txn commit failed");

			return true;
		}

		if(arg0.contains("ROLLBACK"))
		{
			proxy.abort(this.id);
		}

		return true;
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
