package client.proxy;


import client.jdbc.ConnectionFactory;
import client.execution.temporary.Sandbox;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import client.execution.DeterministicQuery;
import common.util.ExitCode;
import common.thrift.CRDTTransaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 02/09/15.
 */
public class BasicProxy implements Proxy
{

	private static final Logger LOG = LoggerFactory.getLogger(BasicProxy.class);

	private final int proxyId;
	private final ProxyConfig proxyConfig;
	private final IProxyNetwork network;
	private Sandbox sandbox;
	private Connection dbConnection;
	private CCJSqlParserManager sqlParser;


	public BasicProxy(final ProxyConfig proxyConfig, int proxyId)
	{
		this.proxyConfig = proxyConfig;
		this.proxyId = proxyId;
		this.network = new ProxyNetwork(proxyConfig);
		this.sqlParser = new CCJSqlParserManager();

		try
		{
			this.dbConnection = ConnectionFactory.getDefaultConnection(proxyConfig);
			this.sandbox = new Sandbox(this.proxyId, this.dbConnection, this.sqlParser);
		} catch(SQLException e)
		{
			LOG.error("failed to create Sandbox for proxy with id {}", this.proxyId, e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SANDBOX_INIT_FAILED);
		}
	}

	@Override
	public int assignConnectionId()
	{
		return this.proxyId;
	}

	@Override
	public void abort(int connectionId)
	{
		this.sandbox.endTransaction();
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		this.sandbox.setReadOnlyMode(readOnly);
	}

	@Override
	public void commit(int connectionId) throws SQLException
	{
		// if read-only, just return
		if(this.sandbox.isReadOnlyMode())
			return;

		CRDTTransaction transaction = this.sandbox.getTransaction();

		boolean commitDecision = this.network.commitOperation(transaction,
				this.proxyConfig.getReplicatorConfig());

		this.sandbox.endTransaction();

		if(!commitDecision)
			throw new SQLException("commit on main storage failed");

	}

	@Override
	public void closeTransaction(int connectionId) throws SQLException
	{
		this.commit(connectionId);
	}

	@Override
	public ResultSet executeQuery(String op, int connectionId) throws SQLException
	{
		return this.sandbox.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op, int connectionId) throws SQLException
	{
		String[] deterministicOps;

		try
		{
			deterministicOps = DeterministicQuery.makeToDeterministic(this.dbConnection, this.sqlParser, op);
		} catch(JSQLParserException e)
		{
			throw new SQLException("parser exception");
		}

		int result = 0;

		for(String updateStr : deterministicOps)
		{
			int counter;
			counter = this.sandbox.executeUpdate(updateStr);
			result += counter;
		}

		return result;
	}

}
