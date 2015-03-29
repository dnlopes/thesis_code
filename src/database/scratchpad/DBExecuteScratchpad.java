package database.scratchpad;


import database.jdbc.ConnectionFactory;
import database.jdbc.Result;
import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBUpdateResult;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import network.proxy.IProxyNetwork;
import network.proxy.ProxyConfig;
import org.apache.thrift.TException;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import runtime.operation.DBSingleOperation;
import runtime.operation.ShadowOperation;
import runtime.txn.Transaction;
import runtime.txn.TransactionIdentifier;
import runtime.txn.TransactionWriteSet;
import util.ExitCode;
import util.defaults.Configuration;
import util.defaults.ScratchpadDefaults;
import util.thrift.CoordResponseMessage;
import util.thrift.RequestEntry;
import util.thrift.ResponseEntry;

import java.sql.*;
import java.sql.Statement;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchpad implements IDBScratchpad
{

	private final Logger LOG = LoggerFactory.getLogger(DBExecuteScratchpad.class);

	private ProxyConfig proxyConfig;

	private Transaction activeTransaction;
	private StopWatch runtimeWatch;
	private int id;
	private Map<String, IExecuter> executers;

	private boolean readOnly;
	private CCJSqlParserManager parser;

	private Connection defaultConnection;
	private Statement statQ;
	private Statement statU;
	private Statement statBU;
	private boolean batchEmpty;

	private TransactionWriteSet writeSet;

	public DBExecuteScratchpad(int id, ProxyConfig proxyConfig) throws SQLException, ScratchpadException
	{
		this.id = id;
		this.proxyConfig = proxyConfig;
		this.defaultConnection = ConnectionFactory.getDefaultConnection(Configuration.getInstance().getDatabaseName());
		this.executers = new HashMap<>();
		this.readOnly = false;
		this.batchEmpty = true;
		this.parser = new CCJSqlParserManager();
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();

		this.writeSet = new TransactionWriteSet();
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

		LOG.info("Beggining txn {}", activeTransaction.getTxnId().getValue());
	}

	@Override
	public boolean commitTransaction(IProxyNetwork network)
	{
		this.runtimeWatch.stop();
		LOG.info("txn runtime: {} ms", runtimeWatch.getElapsedTime());

		if(this.readOnly)
		{
			this.activeTransaction.finish();
			LOG.info("txn {} committed in {} ms", this.activeTransaction.getTxnId().getValue(),
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

			// FIXME: this call MUST NOT block, but for now it DOES
			boolean commitDecision = network.commitOperation(this.activeTransaction.getShadowOp(),
					this.proxyConfig.getReplicatorConfig());

			this.activeTransaction.finish();
			commitTimeWatcher.stop();

			if(commitDecision)
			{
				LOG.info("txn commit time {}", commitTimeWatcher.getElapsedTime());
				LOG.info("txn {} full latency: {} ms", this.activeTransaction.getTxnId().getValue(),
						this.activeTransaction.getLatency());
			} else
				LOG.warn("txn {} failed to commit", this.activeTransaction.getTxnId().getValue());

			return commitDecision;
		}

	}

	@Override
	public int getScratchpadId()
	{
		return this.id;
	}

	@Override
	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	public void setNotReadOnly()
	{
		this.readOnly = false;
	}

	@Override
	public Result executeUpdate(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException
	{
		op.parse(this.parser);

		if(!op.isStandardOperation())
		{
			LOG.warn("executing non standard operation: {}", op.toString());
			int count =  this.executeUpdate(op.toString());
			return DBUpdateResult.createResult(count);
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
			return executor.executeTemporaryUpdate(op, this);

		return null;
	}

	@Override
	public ResultSet executeQuery(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException
	{
		op.parse(this.parser);

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

			return executor.executeTemporaryQueryOnSingleTable(op, this);

		} else
		{
			IExecuter[] executors = new DBExecuter[tableName.length];
			for(int i = 0; i < tableName.length; i++)
			{
				executors[i] = this.executers.get(tableName[i][2]);
				if(tableName[i][1] == null)
					tableName[i][1] = executors[i].getAliasTable();
				if(executors[i] == null)
					throw new ScratchpadException("No config for table " + tableName[i][0]);
			}
			//TODO: fix this
			return executors[0].executeTemporaryQueryOnMultTable(op, this, executors, tableName);
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

	@Override
	public void abort() throws SQLException
	{
		this.defaultConnection.rollback();
	}

	@Override
	public void resetScratchpad() throws SQLException
	{
		this.activeTransaction = null;
		this.readOnly = true;
		this.statBU.clearBatch();

		for(IExecuter executer : this.executers.values())
			executer.resetExecuter(this);

		this.executeBatch();
		this.batchEmpty = true;

		this.writeSet.resetWriteSet();
	}

	@Override
	public TransactionWriteSet createTransactionWriteSet() throws SQLException
	{
		for(IExecuter executer : this.executers.values())
			this.writeSet.addTableWriteSet(executer.getTableName(), executer.getWriteSet());

		return this.writeSet;
	}

	@Override
	public boolean addToWriteSet(DBWriteSetEntry entry)
	{
		//TODO
		return false;
	}

	@Override
	public boolean addToReadSet(DBReadSetEntry readSetEntry)
	{
		//TODO
		return false;
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
			this.writeSet = this.createTransactionWriteSet();
			List<RequestEntry> checkList = this.writeSet.verifyInvariants();

			if(checkList.size() > 0)
			{
				//if we must coordinate then do it here. this is a blocking call
				CoordResponseMessage response = network.checkInvariants(checkList, proxyConfig.getCoordinatorConfig());
				if(!response.isSuccess())
				{
					LOG.warn("coordinator didnt allow txn to commit. Aborting.");
					return;
				}
				List<ResponseEntry> responses = response.getResponses();

				if(responses != null)
					this.writeSet.processCoordinatorResponse(responses);
			}

			this.writeSet.generateMinimalStatements();
			ShadowOperation shadowOp = new ShadowOperation(this.activeTransaction.getTxnId().getValue(),
					this.writeSet.getStatements());
			this.activeTransaction.setReadyToCommit(shadowOp);
		} catch(SQLException | TException e)
		{
			LOG.error("failed to prepare operation {} for commit", this.activeTransaction.getTxnId().getValue(), e);
		}
	}

}
