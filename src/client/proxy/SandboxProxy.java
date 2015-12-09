package client.proxy;


import client.execution.CRDTOperationGenerator;
import client.execution.TransactionContext;
import client.execution.operation.*;
import client.execution.temporary.SQLQueryHijacker;
import client.execution.temporary.scratchpad.BasicScratchpad;
import client.execution.temporary.DBReadOnlyInterface;
import client.execution.temporary.ReadOnlyInterface;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import client.log.TransactionLog;
import client.proxy.network.IProxyNetwork;
import client.proxy.network.SandboxProxyNetwork;
import common.database.SQLBasicInterface;
import common.database.SQLInterface;
import common.nodes.NodeConfig;
import common.util.ConnectionFactory;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.LogicalClock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 02/09/15.
 */
public class SandboxProxy implements Proxy
{

	private static final Logger LOG = LoggerFactory.getLogger(SandboxProxy.class);

	private final int proxyId;
	private final IProxyNetwork network;
	private SQLInterface sqlInterface;
	private boolean readOnly, isRunning;
	private TransactionContext txnContext;
	private ReadOnlyInterface readOnlyInterface;
	private ReadWriteScratchpad scratchpad;
	private List<SQLOperation> operationList;

	public SandboxProxy(final NodeConfig proxyConfig, int proxyId) throws SQLException
	{
		this.proxyId = proxyId;
		this.network = new SandboxProxyNetwork(proxyConfig);
		this.readOnly = false;
		this.isRunning = false;
		this.operationList = new LinkedList<>();

		try
		{
			this.sqlInterface = new SQLBasicInterface(ConnectionFactory.getDefaultConnection(proxyConfig));
			this.readOnlyInterface = new DBReadOnlyInterface(sqlInterface);
			this.scratchpad = new BasicScratchpad(sqlInterface, txnContext);
			this.txnContext = new TransactionContext(sqlInterface);
		} catch(SQLException e)
		{
			throw new SQLException("failed to create scratchpad environment for proxy: " + e.getMessage());
		}
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		//its the first op from this txn
		if(!isRunning)
			start();

		SQLOperation[] preparedOps;
		long start = System.nanoTime();

		try
		{
			preparedOps = SQLQueryHijacker.pepareOperation(op, txnContext);
			long estimated = System.nanoTime() - start;
			this.txnContext.addToParsingTime(estimated);
		} catch(JSQLParserException e)
		{
			throw new SQLException(e.getMessage());
		}

		if(preparedOps.length != 1)
			throw new SQLException("unexpected number of select queries");

		SQLSelect selectSQL = (SQLSelect) preparedOps[0];

		if(selectSQL.getOpType() != SQLOperationType.SELECT)
			throw new SQLException("expected query op but instead we got an update");

		ResultSet rs;
		long estimated;
		if(readOnly)
		{
			rs = this.readOnlyInterface.executeQuery(selectSQL);
			estimated = System.nanoTime() - start;
			this.txnContext.addSelectTime(estimated);
		} else // we dont measure select times from non-read only txn here. we do it in the lower layers
			rs = this.scratchpad.executeQuery(selectSQL);

		return rs;
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		//its the first op from this txn
		if(!isRunning)
			start();

		if(readOnly)
			throw new SQLException("update statement not acceptable under readonly mode");

		SQLOperation[] preparedOps;

		try
		{
			long start = System.nanoTime();
			preparedOps = SQLQueryHijacker.pepareOperation(op, txnContext);
			long estimated = System.nanoTime() - start;
			this.txnContext.addToParsingTime(estimated);

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser exception");
		}

		int result = 0;

		for(SQLOperation updateOp : preparedOps)
		{
			int counter = this.scratchpad.executeUpdate((SQLWriteOperation) updateOp);
			operationList.add(updateOp);
			result += counter;
		}

		return result;
	}

	@Override
	public TransactionLog getTransactionLog()
	{
		return null;
	}

	@Override
	public int getProxyId()
	{
		return this.proxyId;
	}

	@Override
	public void abort()
	{
		try
		{
			this.sqlInterface.rollback();
		} catch(SQLException e)
		{
			LOG.warn(e.getMessage());
		}

		end();
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	@Override
	public void commit() throws SQLException
	{
		end();
		/*
		long endExec = System.nanoTime();
		this.txnContext.setExecTime(endExec - txnContext.getStartTime());

		// if read-only, just return
		if(readOnly)
		{
			txnContext.printRecord();
			end();
			return;
		}

		//txnRecord.printRecord();
		//TODO commit
		long prepareOpStart = System.nanoTime();
		prepareToCommit();
		long endPrepareOp = System.nanoTime();
		long estimated = endPrepareOp - prepareOpStart;
		txnContext.setPrepareOpTime(estimated);

		Status status = network.commitOperation(txnContext.getPreCompiledTxn());
		estimated = System.nanoTime() - endPrepareOp;
		txnContext.setCommitTime(estimated);

		if(!status.isSuccess())
			throw new SQLException(status.getError());

		txnContext.printRecord();
		end(); */
	}

	@Override
	public void close() throws SQLException
	{
		commit();
	}

	private void prepareToCommit()
	{
		//pre-compile ops
		for(SQLOperation op : operationList)
			CRDTOperationGenerator.generateCrdtOperations((SQLWriteOperation) op,
					LogicalClock.CLOCK_PLACEHOLLDER_WITH_ESCAPED_CHARS, txnContext);
	}

	private void start()
	{
		reset();
		txnContext.setStartTime(System.nanoTime());
		isRunning = true;
	}

	private void end()
	{
		txnContext.setEndTime(System.nanoTime());
		isRunning = false;
	}

	private void reset()
	{
		txnContext.clear();
		operationList.clear();

		try
		{
			scratchpad.clearScratchpad();
		} catch(SQLException e)
		{
			LOG.warn("failed to clean scratchpad tables");
		}
	}

}
