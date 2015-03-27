package database.jdbc;


import database.jdbc.util.DBUpdateResult;
import database.scratchpad.ScratchpadException;
import net.sf.jsqlparser.JSQLParserException;
import network.proxy.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.MyShadowOpCreator;
import runtime.operation.DBSingleOperation;
import runtime.txn.TransactionIdentifier;
import util.MissingImplementationException;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.Executor;


/**
 * Created by dnlopes on 04/03/15.
 */
public class CRDTPreparedStatement implements PreparedStatement
{

	private static final Logger LOG = LoggerFactory.getLogger(CRDTPreparedStatement.class);

	String sql;
	int[] argPos;
	String[] vals;
	private MyShadowOpCreator shdOpCreator;
	private TransactionIdentifier txnId;

	private Proxy proxy;

	protected CRDTPreparedStatement(String sql, Proxy proxy, MyShadowOpCreator creator, TransactionIdentifier txnId)
	{
		this.proxy = proxy;
		this.sql = sql;
		this.shdOpCreator = creator;
		this.txnId = txnId;
		init(0, 0);
	}

	private void init(int pos, int count)
	{
		int npos = sql.indexOf('?', pos);
		if(npos == -1)
		{
			argPos = new int[count];
			vals = new String[count];
			return;
		}
		init(npos + 1, count + 1);
		argPos[count] = npos;
	}

	private String generateStatement()
	{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < vals.length; i++)
		{
			buffer.append(sql.substring(i == 0 ? 0 : argPos[i - 1] + 1, argPos[i]));
			buffer.append(vals[i]);
		}
		buffer.append(sql.substring(vals.length > 0 ? argPos[argPos.length - 1] + 1 : 0));
		//Debug.println( "STAT = " + buffer.toString());
		return buffer.toString();
	}

	@Override
	public ResultSet executeQuery() throws SQLException
	{
		String arg0 = this.generateStatement();

		if(this.txnId.getValue() == TransactionIdentifier.DEFAULT_VALUE)
			this.proxy.beginTransaction(txnId);

		ResultSet rs;
		try
		{
			DBSingleOperation dbOp = new DBSingleOperation(arg0);
			rs = proxy.executeQuery(dbOp, this.txnId);

		} catch(JSQLParserException | ScratchpadException e)
		{
			e.printStackTrace();
			LOG.error("failed to execute: {}", arg0, e);
			throw new SQLException(e);
		}

		LOG.trace("query statement executed properly");
		return rs;
	}

	@Override
	public int executeUpdate() throws SQLException
	{
		String arg0 = this.generateStatement();

		if(this.txnId.getValue() == TransactionIdentifier.DEFAULT_VALUE)
			this.proxy.beginTransaction(txnId);

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
				Result tempRes = this.proxy.executeUpdate(dbOp, this.txnId);
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
		return finalRes.getUpdateResult();
	}

	@Override
	public void setDate(int pos, Date val) throws SQLException
	{
		//already implemented
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		vals[pos - 1] = "'" + sdf.format(val) + "'";
	}

	@Override
	public void setDouble(int pos, double val) throws SQLException
	{
		//already implemented
		vals[pos - 1] = "" + val;

	}

	@Override
	public void setFloat(int arg0, float arg1) throws SQLException
	{
		//already implemented
		vals[arg0 - 1] = "" + arg1;
	}

	@Override
	public void setInt(int pos, int val) throws SQLException
	{
		//already implemented
		vals[pos - 1] = "" + val;
	}

	@Override
	public void setLong(int pos, long val) throws SQLException
	{
		//already implemented
		vals[pos - 1] = "" + val;
	}

	@Override
	public void setString(int pos, String val) throws SQLException
	{
		//already implemented
		vals[pos - 1] = "'" + val + "'";
	}

	@Override
	public void setTimestamp(int pos, Timestamp val) throws SQLException
	{
		//already implemented
		vals[pos - 1] = "'" + val + "'";
	}


/*
	NOT IMPLEMENTED METHODS START HERE
*/

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
	public ResultSet executeQuery(String arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException
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

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void addBatch() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearParameters() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean execute() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setArray(int arg0, Array arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBlob(int arg0, Blob arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBlob(int arg0, InputStream arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBoolean(int arg0, boolean arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setByte(int arg0, byte arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setBytes(int arg0, byte[] arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setClob(int arg0, Clob arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setClob(int arg0, Reader arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNClob(int arg0, NClob arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNClob(int arg0, Reader arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNString(int arg0, String arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNull(int arg0, int arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNull(int arg0, int arg1, String arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setObject(int arg0, Object arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setRef(int arg0, Ref arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setRowId(int arg0, RowId arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setShort(int arg0, short arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setTime(int arg0, Time arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setURL(int arg0, URL arg1) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	/**
	 * @deprecated
	 */ public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException
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

	public void abort(Executor executor) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public int getNetworkTimeout() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public String getSchema() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	public void setSchema(String schema) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

}

