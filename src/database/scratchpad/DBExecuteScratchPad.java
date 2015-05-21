package database.scratchpad;


import database.jdbc.ConnectionFactory;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;
import network.proxy.IProxyNetwork;
import network.proxy.ProxyConfig;
import org.apache.thrift.TException;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import runtime.operation.DBSingleOperation;
import runtime.operation.Operation;
import runtime.txn.Transaction;
import runtime.txn.TransactionIdentifier;
import util.ExitCode;
import util.defaults.ScratchpadDefaults;
import util.thrift.*;

import java.sql.*;
import java.sql.Statement;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchPad implements IDBScratchPad
{

	private static final Logger LOG = LoggerFactory.getLogger(DBExecuteScratchPad.class);

	private ProxyConfig proxyConfig;
	private Transaction activeTransaction;
	private StopWatch runtimeWatch;
	private int id;
	private Map<String, IExecuter> executers;
	private CCJSqlParserManager parser;
	private Connection defaultConnection;
	private Statement statQ;
	private Statement statU;
	private Statement statBU;
	private boolean batchEmpty;

	public DBExecuteScratchPad(int id, ProxyConfig proxyConfig) throws SQLException, ScratchpadException
	{
		this.id = id;
		this.proxyConfig = proxyConfig;
		this.defaultConnection = ConnectionFactory.getDefaultConnection(proxyConfig);
		this.executers = new HashMap<>();
		this.batchEmpty = true;
		this.parser = new CCJSqlParserManager();
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();
		this.runtimeWatch = new StopWatch("txn runtime");

		this.createDBExecuters();
	}

	@Override
	public void startTransaction(TransactionIdentifier txnId)
	{
		try
		{
			this.resetScratchpad();
		} catch(SQLException e)
		{
			LOG.error("failed to clean scratchpad before starting transaction", e);
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.SCRATCHPAD_CLEANUP_ERROR);
		}
		this.activeTransaction = new Transaction(txnId);
		this.runtimeWatch.start();
		this.activeTransaction.start();

		LOG.trace("Beggining txn {}", activeTransaction.getTxnId().getValue());
	}

	@Override
	public boolean commitTransaction(IProxyNetwork network)
	{
		this.runtimeWatch.stop();
		LOG.debug("txn runtime: {} ms", runtimeWatch.getElapsedTime());

		if(this.activeTransaction.isReadOnly())
		{
			this.activeTransaction.finish();
			LOG.info("txn {} committed in {} ms (read-only)", this.activeTransaction.getTxnId().getValue(),
					this.activeTransaction.getLatency());
			return true;
		} else
		{
			StopWatch commitTimeWatcher = new StopWatch("commit time watcher");
			commitTimeWatcher.start();
			this.prepareToCommit(network);

			// something went wrong
			// commit fails
			if(!this.activeTransaction.isReadyToCommit())
				return false;

			boolean commitDecision = network.commitOperation(this.activeTransaction.getShadowOp(),
					this.proxyConfig.getReplicatorConfig());

			this.activeTransaction.finish();
			commitTimeWatcher.stop();

			if(commitDecision)
			{
				LOG.debug("txn commit time {}", commitTimeWatcher.getElapsedTime());
				LOG.info("txn {} committed in {} ms", this.activeTransaction.getTxnId().getValue(),
						this.activeTransaction.getLatency());

			} else
				LOG.warn("txn {} failed to commit on main storage", this.activeTransaction.getTxnId().getValue());

			return commitDecision;
		}
	}

	@Override
	public int getScratchpadId()
	{
		return this.id;
	}

	@Override
	public int executeUpdate(DBSingleOperation op) throws SQLException
	{
		try
		{
			op.parse(this.parser);
		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(op.isQuery())
			RuntimeHelper.throwRunTimeException("query operation expected", ExitCode.UNEXPECTED_OP);

		String[][] tableName = op.targetTable();

		if(tableName.length > 1)
			RuntimeHelper.throwRunTimeException("multi-table update not expected", ExitCode.MULTI_TABLE_UPDATE);

		IExecuter executor = this.executers.get(tableName[0][2]);
		if(executor == null)
		{
			LOG.error("executor for table {} not found", tableName[0][2]);
			RuntimeHelper.throwRunTimeException("could not find a proper executor for this operation",
					ExitCode.EXECUTOR_NOT_FOUND);
		} else
			try
			{
				return executor.executeTemporaryUpdate(op, this);
			} catch(ScratchpadException e)
			{
				throw new SQLException("scratchpad error: " + e.getMessage());
			}

		return 0;
	}

	@Override
	public ResultSet executeQuery(DBSingleOperation op) throws SQLException
	{
		try
		{
			op.parse(this.parser);
		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(!op.isQuery())
			RuntimeHelper.throwRunTimeException("query operation expected", ExitCode.UNEXPECTED_OP);

		String[][] tableName = op.targetTable();
		if(tableName.length == 1)
		{
			IExecuter executor = this.executers.get(tableName[0][2]);
			if(executor == null)
			{
				LOG.error("executor for table {} not found", tableName[0][2]);
				RuntimeHelper.throwRunTimeException("could not find a proper executor for this operation",
						ExitCode.EXECUTOR_NOT_FOUND);
			}

			try
			{
				return executor.executeTemporaryQueryOnSingleTable((Select) op.getStatement(), this);
			} catch(ScratchpadException e)
			{
				throw new SQLException("scratchpad error: " + e.getMessage());
			}

		} else
		{
			IExecuter[] executors = new DBExecuter[tableName.length];
			for(int i = 0; i < tableName.length; i++)
			{
				executors[i] = this.executers.get(tableName[i][2]);
				if(tableName[i][1] == null)
					tableName[i][1] = executors[i].getAliasTable();
				if(executors[i] == null)
					throw new SQLException("scratchpad error: executor not found");
			}
			try
			{
				return executors[0].executeTemporaryQueryOnMultTable((Select) op.getStatement(), this, executors,
						tableName);
			} catch(ScratchpadException e)
			{
				throw new SQLException("scratchpad error: " + e.getMessage());
			}
		}
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		return this.statQ.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		return this.statU.executeUpdate(op);
	}

	@Override
	public void addToBatchUpdate(String op) throws SQLException
	{
		this.statBU.addBatch(op);
		this.batchEmpty = false;
	}

	@Override
	public int executeBatch() throws SQLException
	{
		if(this.batchEmpty)
		{
			LOG.warn("trying to executeUpdate an empty batch for pad {}", this.getScratchpadId());
			// -1 is the default error
			return -1;
		}

		int[] res = statBU.executeBatch();
		int finalResult = 0;

		for(int i = 0; i < res.length; i++)
			finalResult += res[i];

		statBU.clearBatch();
		batchEmpty = true;
		return finalResult;
	}

	private void resetScratchpad() throws SQLException
	{
		this.activeTransaction = null;
		this.statBU.clearBatch();

		for(IExecuter executer : this.executers.values())
			executer.resetExecuter(this);

		this.executeBatch();
		this.batchEmpty = true;
	}

	@Override
	public Transaction getActiveTransaction()
	{
		return this.activeTransaction;
	}

	private void createDBExecuters() throws SQLException, ScratchpadException
	{
		DatabaseMetaData metadata = this.defaultConnection.getMetaData();
		String[] types = {"TABLE"};
		ResultSet tblSet = metadata.getTables(null, null, "%", types);

		ArrayList<String> tempTables = new ArrayList<>();
		while(tblSet.next())
		{
			String tableName = tblSet.getString(3);
			if(tableName.startsWith(ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX))
				continue;

			tempTables.add(tableName);
		}

		Collections.sort(tempTables);

		for(int i = 0; i < tempTables.size(); i++)
		{
			String tableName = tempTables.get(i);
			IExecuter executor = new DBExecuter(i, tableName);
			executor.setup(metadata, this);
			this.executers.put(tableName.toUpperCase(), executor);
			this.defaultConnection.commit();
		}
	}

	private void prepareToCommit(IProxyNetwork network)
	{
		LOG.trace("preparing to commit txn {}", this.activeTransaction.getTxnId().getValue());

		try
		{
			CoordinatorRequest req = new CoordinatorRequest();

			req.setDeltaValues(new ArrayList<ApplyDelta>());
			req.setRequests(new ArrayList<RequestValue>());
			req.setUniqueValues(new ArrayList<UniqueValue>());

			for(Operation op : this.activeTransaction.getTxnOps())
				op.createRequestsToCoordinate(req);

			if(req.getDeltaValues().size() > 0 || req.getRequests().size() > 0 || req.getUniqueValues().size() > 0)
			{
				//if we must coordinate then do it here. this is a blocking call
				CoordinatorResponse response = network.sendRequestToCoordinator(req, proxyConfig
						.getCoordinatorConfig());
				if(!response.isSuccess())
				{
					LOG.warn("coordinator didnt allow txn to commit: {}", response.getErrorMessage());
					return;
				}
				List<RequestValue> requestValues = response.getRequestedValues();

				if(requestValues != null)
					this.activeTransaction.updatedWithRequestedValues(requestValues);
			}

			this.activeTransaction.generateShadowOperation();
		} catch(TException e)
		{
			LOG.error("failed to prepare operation {} for commit", this.activeTransaction.getTxnId().getValue(), e);
		}
	}
}
