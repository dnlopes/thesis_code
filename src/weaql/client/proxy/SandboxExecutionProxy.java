package weaql.client.proxy;


import weaql.client.execution.CRDTOperationGenerator;
import weaql.client.execution.TransactionContext;
import weaql.client.operation.*;
import weaql.client.execution.temporary.DBReadOnlyInterface;
import weaql.client.execution.temporary.ReadOnlyInterface;
import weaql.client.execution.temporary.SQLQueryHijacker;
import weaql.client.execution.temporary.WriteSet;
import weaql.client.execution.temporary.scratchpad.*;
import weaql.client.proxy.log.TransactionLog;
import weaql.client.proxy.log.TransactionLogEntry;
import weaql.client.proxy.network.IProxyNetwork;
import weaql.client.proxy.network.SandboxProxyNetwork;
import weaql.common.database.Record;
import weaql.common.database.SQLBasicInterface;
import weaql.common.database.SQLInterface;
import weaql.common.database.field.DataField;
import weaql.common.database.table.DatabaseTable;
import weaql.common.database.util.DatabaseCommon;
import weaql.common.nodes.NodeConfig;
import weaql.common.thrift.CRDTPreCompiledTransaction;
import weaql.common.thrift.Status;
import weaql.common.util.ConnectionFactory;
import weaql.common.util.exception.SocketConnectionException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weaql.server.util.LogicalClock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by dnlopes on 02/09/15.
 */
public class SandboxExecutionProxy implements Proxy
{

	private static final Logger LOG = LoggerFactory.getLogger(SandboxExecutionProxy.class);

	private final CCJSqlParserManager parserManager;
	private final int proxyId;
	private final IProxyNetwork network;
	private SQLInterface sqlInterface;
	private boolean readOnly, isRunning;
	private TransactionContext txnContext;
	private ReadOnlyInterface readOnlyInterface;
	private IDBScratchpad scratchpad;
	private List<SQLOperation> operationList;
	private TransactionLog transactionLog;

	public SandboxExecutionProxy(final NodeConfig proxyConfig, int proxyId) throws SQLException
	{
		this.proxyId = proxyId;
		this.network = new SandboxProxyNetwork(proxyConfig);
		this.readOnly = false;
		this.isRunning = false;
		this.operationList = new LinkedList<>();
		this.transactionLog = new TransactionLog();
		this.parserManager = new CCJSqlParserManager();

		try
		{
			this.sqlInterface = new SQLBasicInterface(ConnectionFactory.getDefaultConnection(proxyConfig));
			this.readOnlyInterface = new DBReadOnlyInterface(sqlInterface);
			this.txnContext = new TransactionContext(sqlInterface);
			this.scratchpad = new DBScratchpad(sqlInterface, txnContext);
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

		long start = System.nanoTime();

		if(readOnly)
		{
			//TODO filter deleted records in this case
			ResultSet rs = this.readOnlyInterface.executeQuery(op);
			long estimated = System.nanoTime() - start;
			this.txnContext.addSelectTime(estimated);
			return rs;
		} else
		{
			SQLOperation[] preparedOps;

			try
			{
				preparedOps = SQLQueryHijacker.pepareOperation(op, txnContext, parserManager);
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
			preparedOps = SQLQueryHijacker.pepareOperation(op, txnContext, parserManager);
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

					if(field.isLwwField())
						insertedRecord.addData(updatedEntry.getKey(), updatedEntry.getValue());
					else if(field.isDeltaField())
					{
						double initValue = Double.parseDouble(insertedRecord.getData(field.getFieldName()));
						double diffValue = DatabaseCommon.extractDelta(updatedVersion.getData(field.getFieldName()),
								field.getFieldName());

						double finalValue = initValue + diffValue;
						insertedRecord.addData(field.getFieldName(), String.valueOf(finalValue));
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

			// use cached record as baseline, then override the changed columns
			// that are present in the updated version of the record
			for(Map.Entry<String, String> updatedEntry : updatedRecord.getRecordData().entrySet())
			{
				DataField field = table.getField(updatedEntry.getKey());

				if(field.isPrimaryKey())
					continue;

				cachedRecord.addData(updatedEntry.getKey(), updatedEntry.getValue());
			}

			if(table.isChildTable())
				CRDTOperationGenerator.updateChildRow(cachedRecord, clockPlaceHolder, txnContext);
			else
				CRDTOperationGenerator.updateRow(cachedRecord, clockPlaceHolder, txnContext);
		}

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
