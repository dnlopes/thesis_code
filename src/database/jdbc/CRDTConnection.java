package database.jdbc;

import database.scratchpad.ExecutePadFactory;
import database.scratchpad.ExecuteScratchpad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.*;
import util.ExitCode;
import util.MissingImplementationException;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


/**
 * Created by dnlopes on 10/02/15.
 */
public class CRDTConnection implements Connection
{

	static final Logger LOG = LoggerFactory.getLogger(CRDTConnection.class);

	private TransactionInfo txnInfo;
	private Operation shadowOp;
	private ExecuteScratchpad pad;
	private MyShadowOpCreator shdOpCreator;

	public CRDTConnection() throws SQLException
	{
		this.txnInfo = new TransactionInfo();
		this.pad = ExecutePadFactory.getInstance().getScratchpad();
		this.shdOpCreator = new MyShadowOpCreator(Configuration.SCHEMA_FILE, 1, 1);
	}

	@Override
	public Statement createStatement() throws SQLException
	{
		//TODO
		if(! this.txnInfo.hasBegun())
			this.txnInfo.beginTxn();

		return new CRDTStatement(this.txnInfo, this.pad, this.shdOpCreator);
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		//TODO
		if(! this.txnInfo.hasBegun())
			this.txnInfo.beginTxn();

		return new CRDTPreparedStatement(sql, this.txnInfo, this.pad, this.shdOpCreator);
	}

	@Override
	public void commit() throws SQLException
	{
		//TODO
		/*
		System.out.println("Should not come here in Sifter");
		inTx = false;
		if( ! proxy.commit(txId, op, color)){
			internalAborted = true;
			throw new SQLException( "commit failed");
		}*/

	}

	@Override
	public void rollback() throws SQLException
	{
		//TODO
		   /*
		inTx = false;
		if(internalAborted == false)
			proxy.abort( txId);
		if(shdOp != null && !shdOp.isEmpty()) {
			shdOp.clear();
		}*/
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		if(autoCommit)
			throw new SQLException("autocommit not supported");
	}


/*
	NOT IMPLEMENTED METHODS START HERE
*/

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public String nativeSQL(String sql) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean getAutoCommit() throws SQLException
	{
		return false;
	}

	@Override
	public void close() throws SQLException
	{
		//TODO review
	}

	@Override
	public int getTransactionIsolation() throws SQLException
	{
		return 0;
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isReadOnly() throws SQLException
	{
		return false;
	}

	@Override
	public void setCatalog(String catalog) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public String getCatalog() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void clearWarnings() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setHoldability(int holdability) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getHoldability() throws SQLException
	{
		return 0;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
											  int resultSetHoldability) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
										 int resultSetHoldability) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Clob createClob() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Blob createBlob() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public NClob createNClob() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isValid(int timeout) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException
	{
		System.exit(ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException
	{
		System.exit(ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public String getClientInfo(String name) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Properties getClientInfo() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setSchema(String schema) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public String getSchema() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void abort(Executor executor) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public int getNetworkTimeout() throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		throw new MissingImplementationException("missing implementation");
	}
}
