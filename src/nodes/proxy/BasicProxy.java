package nodes.proxy;


import database.jdbc.ConnectionFactory;
import database.scratchpad.Sandbox;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import nodes.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import runtime.Transaction;
import runtime.operation.ShadowOperation;
import runtime.transformer.DeterministicQuery;
import util.ExitCode;
import util.thrift.CoordinatorRequest;
import util.thrift.ThriftShadowTransaction;

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

	public BasicProxy(NodeConfig proxyConfig, int proxyId)
	{
		this.proxyConfig = (ProxyConfig) proxyConfig;
		this.proxyId = proxyId;
		this.network = new ProxyNetwork((ProxyConfig) proxyConfig);
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
		// do nothing
	}

	@Override
	public void commit(int connectionId) throws SQLException
	{
		Transaction transaction = this.sandbox.getActiveTransaction();

		// if read-only, just return
		if(transaction.isReadOnly())
			return;

		CoordinatorRequest request = this.createCoordinatorRequest(transaction);

		ThriftShadowTransaction shadowTransaction = RuntimeUtils.encodeShadowTransaction(transaction);

		if(request.isRequiresCoordination())
			shadowTransaction.setRequestToCoordinator(request);

		boolean commitDecision = this.network.commitOperation(shadowTransaction,
				this.proxyConfig.getReplicatorConfig());

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

	private CoordinatorRequest createCoordinatorRequest(Transaction transaction) throws SQLException
	{
		CoordinatorRequest req = new CoordinatorRequest();

		req.setRequiresCoordination(false);

		for(ShadowOperation op : transaction.getShadowOperations())
			op.createRequestsToCoordinate(req);

		return req;
	}

}
