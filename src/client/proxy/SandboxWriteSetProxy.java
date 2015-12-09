package client.proxy;


import client.execution.CRDTOperationGenerator;
import client.execution.TransactionContext;
import client.execution.operation.*;
import client.execution.temporary.DBReadOnlyInterface;
import client.execution.temporary.ReadOnlyInterface;
import client.execution.temporary.SQLQueryHijacker;
import client.execution.temporary.WriteSet;
import client.execution.temporary.scratchpad.*;
import client.log.TransactionLog;
import client.log.TransactionLogEntry;
import client.proxy.network.IProxyNetwork;
import client.proxy.network.SandboxProxyNetwork;
import common.database.Record;
import common.database.SQLBasicInterface;
import common.database.SQLInterface;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.nodes.NodeConfig;
import common.thrift.CRDTPreCompiledTransaction;
import common.thrift.Status;
import common.util.ConnectionFactory;
import common.util.exception.SocketConnectionException;
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
public class SandboxWriteSetProxy implements Proxy
{

	private static final Logger LOG = LoggerFactory.getLogger(SandboxWriteSetProxy.class);

	private final int proxyId;
	private final IProxyNetwork network;
	private SQLInterface sqlInterface;
	private boolean readOnly, isRunning;
	private TransactionContext txnContext;
	private ReadOnlyInterface readOnlyInterface;
	private ReadWriteScratchpad scratchpad;
	private List<SQLOperation> operationList;
	private TransactionLog transactionLog;

	public SandboxWriteSetProxy(final NodeConfig proxyConfig, int proxyId) throws SQLException
	{
		this.proxyId = proxyId;
		this.network = new SandboxProxyNetwork(proxyConfig);
		this.readOnly = false;
		this.isRunning = false;
		this.operationList = new LinkedList<>();
		this.transactionLog = new TransactionLog();

		try
		{
			this.sqlInterface = new SQLBasicInterface(ConnectionFactory.getDefaultConnection(proxyConfig));
			this.readOnlyInterface = new DBReadOnlyInterface(sqlInterface);
			this.txnContext = new TransactionContext(sqlInterface);
			this.scratchpad = new WriteSetScratchpad(sqlInterface, txnContext);
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
		return transactionLog;
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
		long endExec = System.nanoTime();
		this.txnContext.setExecTime(endExec - txnContext.getStartTime());

		// if read-only, just return
		if(readOnly)
		{
			end();
			return;
		}

		CRDTPreCompiledTransaction crdtTxn = prepareToCommit();
		long estimated = System.nanoTime() - endExec;
		txnContext.setPrepareOpTime(estimated);

		long commitStart = System.nanoTime();

		if(!crdtTxn.isSetOpsList())
		{
			estimated = System.nanoTime() - commitStart;
			txnContext.setCommitTime(estimated);
			end();
			return;
		}

		Status status = null;
		try
		{
			status = network.commitOperation(crdtTxn);
		} catch(SocketConnectionException e)
		{
			throw new SQLException(e.getMessage());
		} finally
		{
			estimated = System.nanoTime() - commitStart;
			txnContext.setCommitTime(estimated);
			end();
		}

		if(!status.isSuccess())
			throw new SQLException(status.getError());
	}

	@Override
	public void close() throws SQLException
	{
		commit();
	}

	private CRDTPreCompiledTransaction prepareToCommit() throws SQLException
	{
		WriteSet snapshot = scratchpad.getWriteSet();

		Map<String, Record> cache = snapshot.getCachedRecords();
		Map<String, Record> updates = snapshot.getUpdates();
		Map<String, Record> inserts = snapshot.getInserts();
		Map<String, Record> deletes = snapshot.getDeletes();

		String clockPlaceHolder = LogicalClock.CLOCK_PLACEHOLLDER_WITH_ESCAPED_CHARS;

		// take care of INSERTS
		for(Record insertedRecord : inserts.values())
		{
			DatabaseTable table = insertedRecord.getDatabaseTable();
			String pkValueString = insertedRecord.getPkValue().getUniqueValue();

			if(updates.containsKey(pkValueString))
			{
				Record updatedVersion = updates.get(pkValueString);

				// it was inserted and later updated.
				// use inserted record values as base, and then override
				// the columns that are present in the update record
				for(Map.Entry<String, String> updatedEntry : updatedVersion.getRecordData().entrySet())
				{
					DataField field = table.getField(updatedEntry.getKey());

					if(field.isPrimaryKey())
						continue;

					if(field.isLWWField())
						insertedRecord.addData(updatedEntry.getKey(), updatedEntry.getValue());
					else if(field.isDeltaField())
					{
						//TODO
						// calculate delta and merge
					}
				}

				// remove from updates for performance
				// we no longer have to execute a update for this record
				updates.remove(updatedVersion.getPkValue().getUniqueValue());
			}

			if(table.isChildTable())
				CRDTOperationGenerator.insertChildRow(insertedRecord, clockPlaceHolder, txnContext);
			else
				CRDTOperationGenerator.insertRow(insertedRecord, clockPlaceHolder, txnContext);
		}

		for(Record updatedRecord : updates.values())
		{
			DatabaseTable table = updatedRecord.getDatabaseTable();

			if(!cache.containsKey(updatedRecord.getPkValue().getUniqueValue()))
				throw new SQLException("updated record missing from cache");

			Record cachedRecord = cache.get(updatedRecord.getPkValue().getUniqueValue());

			// use cached record was baseline, then override the changed columns
			// the columns that are present in the update record
			for(Map.Entry<String, String> updatedEntry : updatedRecord.getRecordData().entrySet())
			{
				DataField field = table.getField(updatedEntry.getKey());

				if(field.isPrimaryKey())
					continue;

				if(field.isLWWField())
					cachedRecord.addData(updatedEntry.getKey(), updatedEntry.getValue());
				else if(field.isDeltaField())
				{
					//TODO
					// calculate delta and merge
				}
			}

			if(table.isChildTable())
				CRDTOperationGenerator.updateChildRow(cachedRecord, clockPlaceHolder, txnContext);
			else
				CRDTOperationGenerator.updateRow(cachedRecord, clockPlaceHolder, txnContext);
		}
		// TODO
		// take care of DELETES
		for(Record deletedRecord : deletes.values())
		{
			DatabaseTable table = deletedRecord.getDatabaseTable();

			if(table.isParentTable())
				CRDTOperationGenerator.deleteParentRow(deletedRecord, clockPlaceHolder, txnContext);
			else
				CRDTOperationGenerator.deleteRow(deletedRecord, clockPlaceHolder, txnContext);
		}

		return txnContext.getPreCompiledTxn();
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
		TransactionLogEntry entry = new TransactionLogEntry(proxyId, txnContext.getSelectsTime(),
				txnContext.getUpdatesTime(), txnContext.getInsertsTime(), txnContext.getDeletesTime(),
				txnContext.getParsingTime(), txnContext.getExecTime(), txnContext.getCommitTime(),
				txnContext.getPrepareOpTime(), txnContext.getLoadFromMainTime());

		transactionLog.addEntry(entry);
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
