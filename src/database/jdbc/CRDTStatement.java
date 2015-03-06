package database.jdbc;

import java.sql.*;


/**
 * Created by dnlopes on 04/03/15.
 */
public class CRDTStatement implements Statement
{

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void addBatch(String arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void cancel() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void clearBatch() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void close() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean execute(String arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean execute(String arg0, int arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int[] executeBatch() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		//TODO: implement
		return null;
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
	{
		//TODO: implement
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getFetchDirection() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getFetchSize() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getMaxFieldSize() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getMaxRows() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean getMoreResults() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean getMoreResults(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getQueryTimeout() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public ResultSet getResultSet() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getResultSetConcurrency() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getResultSetHoldability() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getResultSetType() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public int getUpdateCount() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public boolean isPoolable() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setCursorName(String arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setMaxFieldSize(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setMaxRows(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setPoolable(boolean arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	@Override
	public void setQueryTimeout(int arg0) throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	public void closeOnCompletion() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}

	public boolean isCloseOnCompletion() throws SQLException
	{
		throw new RuntimeException("missing method implementation");
	}
}
