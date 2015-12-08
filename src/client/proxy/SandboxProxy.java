package client.proxy;


import applications.util.SymbolsManager;
import client.execution.CRDTOperationGenerator;
import client.execution.SymbolsContext;
import client.execution.TransactionContext;
import client.execution.operation.*;
import client.execution.temporary.scratchpad.BasicScratchpad;
import client.execution.temporary.scratchpad.DBReadOnlyInterface;
import client.execution.temporary.scratchpad.ReadOnlyInterface;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import client.execution.temporary.scratchpad.agent.IExecutorAgent;
import client.proxy.network.IProxyNetwork;
import client.proxy.network.SandboxProxyNetwork;
import common.database.SQLBasicInterface;
import common.database.SQLInterface;
import common.database.constraints.unique.UniqueConstraint;
import common.database.field.DataField;
import common.database.util.DatabaseCommon;
import common.database.util.PrimaryKeyValue;
import common.database.value.FieldValue;
import common.nodes.NodeConfig;
import common.thrift.Status;
import common.thrift.ThriftUtils;
import common.thrift.UniqueValue;
import common.util.ConnectionFactory;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.util.LogicalClock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	private SymbolsContext symbolsManager;
	private TransactionContext txnContext;
	private ReadOnlyInterface readOnlyInterface;
	private ReadWriteScratchpad scratchpad;
	private SQLDeterministicTransformer transformer;
	private List<SQLOperation> operationList;

	public SandboxProxy(final NodeConfig proxyConfig, int proxyId) throws SQLException
	{
		this.proxyId = proxyId;
		this.network = new SandboxProxyNetwork(proxyConfig);
		this.txnContext = new TransactionContext();
		this.symbolsManager = new SymbolsContext();
		this.readOnly = false;
		this.isRunning = false;
		this.transformer = new SQLDeterministicTransformer();
		this.operationList = new LinkedList<>();

		try
		{
			this.sqlInterface = new SQLBasicInterface(ConnectionFactory.getDefaultConnection(proxyConfig));
			this.readOnlyInterface = new DBReadOnlyInterface(sqlInterface);
			this.scratchpad = new BasicScratchpad(sqlInterface, txnContext);
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
			preparedOps = this.transformer.pepareOperation(op);
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
			preparedOps = this.transformer.pepareOperation(op);
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
		symbolsManager.clear();

		try
		{
			scratchpad.clearScratchpad();
		} catch(SQLException e)
		{
			LOG.warn("failed to clean scratchpad tables");
		}
	}

	public final class SQLDeterministicTransformer
	{

		private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

		public SQLOperation[] pepareOperation(String sqlOpString) throws JSQLParserException, SQLException
		{
			SQLOperation sqlOp = SQLOperation.parseSQLOperation(sqlOpString);

			if(sqlOp.getOpType() == SQLOperationType.INSERT)
				return prepareInsertOperation((SQLInsert) sqlOp);
			else if(sqlOp.getOpType() == SQLOperationType.UPDATE)
				return prepareUpdateOperation((SQLUpdate) sqlOp);
			else if(sqlOp.getOpType() == SQLOperationType.DELETE)
				return prepareDeleteOperation((SQLDelete) sqlOp);
			else if(sqlOp.getOpType() == SQLOperationType.SELECT)
				return prepareSelectOperation((SQLSelect) sqlOp);
			else
				throw new JSQLParserException("unkown SQL operation type");

		}

		private SQLOperation[] prepareSelectOperation(SQLSelect selectSQL)
		{
			SQLOperation[] select = new SQLOperation[1];
			select[0] = selectSQL;
			return select;
		}

		private SQLOperation[] prepareDeleteOperation(SQLDelete deleteSQL) throws JSQLParserException
		{
			if(deleteSQL.isPrimaryKeyMissingFromWhere())
			{
				StringBuilder sqlQuery = new StringBuilder("SELECT ");
				sqlQuery.append(deleteSQL.getPk().getQueryClause());
				sqlQuery.append(" FROM ").append(deleteSQL.getDbTable().getName());
				sqlQuery.append(" WHERE ").append(deleteSQL.getDelete().getWhere().toString());
				List<SQLOperation> deleteOps = new LinkedList<>();

				try
				{
					ResultSet rs = sqlInterface.executeQuery(sqlQuery.toString());

					while(rs.next())
					{
						SQLDelete aDelete = (SQLDelete) deleteSQL.duplicate();

						PrimaryKeyValue pkValue = new PrimaryKeyValue(deleteSQL.getDbTable().getName());

						for(DataField pkField : deleteSQL.getPk().getPrimaryKeyFields().values())
						{
							FieldValue fValue = new FieldValue(pkField, rs.getString(pkField.getFieldName()).trim());
							pkValue.addFieldValue(fValue);
						}

						pkValue.preparePrimaryKey();
						aDelete.setPrimaryKey(pkValue);
						deleteOps.add(aDelete);
					}

				} catch(SQLException e)
				{
					throw new JSQLParserException("failed to retrieve pks values for non-deterministic update");
				}

				return deleteOps.toArray(new SQLOperation[deleteOps.size()]);

			} else
			{
				//TODO if no pk is missing, capture here the PKvalue from the statement
				// send email to ask how to do that
				SQLOperation[] delete = new SQLOperation[1];
				delete[0] = deleteSQL;
				return delete;
			}
		}

		private SQLOperation[] prepareUpdateOperation(SQLUpdate updateSQL) throws JSQLParserException
		{
			Iterator colIt = updateSQL.getUpdate().getColumns().iterator();
			Iterator valueIt = updateSQL.getUpdate().getExpressions().iterator();

			while(colIt.hasNext())
			{
				String column = colIt.next().toString().trim();
				String value = valueIt.next().toString().trim();

				if(value.equalsIgnoreCase("NOW()") || value.equalsIgnoreCase("NOW") || value.equalsIgnoreCase(
						"CURRENT_TIMESTAMP") || value.equalsIgnoreCase("CURRENT_TIMESTAMP()") || value
						.equalsIgnoreCase("CURRENT_DATE"))
					value = "'" + DatabaseCommon.CURRENTTIMESTAMP(DATE_FORMAT) + "'";

				if(value.contains("SELECT") || value.contains("select"))
					throw new JSQLParserException("nested select not yet supported");

				updateSQL.addRecordEntry(column, value);

				if(colIt.hasNext())
					updateSQL.prepareForNextInput();
			}

			if(updateSQL.isPrimaryKeyMissingFromWhere())
			{
				//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
				//it not, please first query
				StringBuilder sqlQuery = new StringBuilder("SELECT ");
				sqlQuery.append(updateSQL.getPk().getQueryClause());
				sqlQuery.append(" FROM ").append(updateSQL.getDbTable().getName());
				sqlQuery.append(" WHERE ").append(updateSQL.getUpdate().getWhere().toString());
				List<SQLOperation> updateOps = new LinkedList<>();

				try
				{
					ResultSet rs = sqlInterface.executeQuery(sqlQuery.toString());

					while(rs.next())
					{
						SQLUpdate anUpdate = (SQLUpdate) updateSQL.duplicate();

						PrimaryKeyValue pkValue = new PrimaryKeyValue(updateSQL.getDbTable().getName());

						for(DataField pkField : updateSQL.getPk().getPrimaryKeyFields().values())
						{
							String cachedContent = rs.getString(pkField.getFieldName());

							if(cachedContent == null)
							{
								int a = 0;
								if(pkField.isStringField())
									cachedContent = "NULL";
								else if(pkField.isDateField())
									cachedContent = IExecutorAgent.Defaults.DEFAULT_DATE_VALUE;
							}

							FieldValue fValue = new FieldValue(pkField, rs.getString(pkField.getFieldName()).trim());
							pkValue.addFieldValue(fValue);
						}

						pkValue.preparePrimaryKey();
						anUpdate.setPrimaryKey(pkValue);
						updateOps.add(anUpdate);
					}

				} catch(SQLException e)
				{
					throw new JSQLParserException("failed to retrieve pks values for non-deterministic update");
				}

				return updateOps.toArray(new SQLOperation[updateOps.size()]);
			} else
			{
				SQLOperation[] update = new SQLOperation[1];
				update[0] = updateSQL;
				return update;
			}
		}

		private SQLOperation[] prepareInsertOperation(SQLInsert insertSQL) throws JSQLParserException, SQLException
		{
			Iterator colIt = insertSQL.getInsert().getColumns().iterator();
			Iterator valueIt = ((ExpressionList) insertSQL.getInsert().getItemsList()).getExpressions().iterator();

			while(colIt.hasNext())
			{
				String column = colIt.next().toString().trim();
				String value = valueIt.next().toString().trim();

				if(value.equalsIgnoreCase("NOW()") || value.equalsIgnoreCase("NOW") || value.equalsIgnoreCase(
						"CURRENT_TIMESTAMP") || value.equalsIgnoreCase("CURRENT_TIMESTAMP()") || value
						.equalsIgnoreCase("CURRENT_DATE"))
					value = "'" + DatabaseCommon.CURRENTTIMESTAMP(DATE_FORMAT) + "'";

				if(value.contains(SymbolsManager.SYMBOL_PREFIX))
				{
					if(symbolsManager.containsSymbol(value)) // get tmp value already generated
					{
						insertSQL.addSymbolEntry(value, column);
						String tmpId = symbolsManager.getSymbolValue(value);
						symbolsManager.linkRecordWithSymbol(insertSQL.getRecord(), value);
						value = tmpId;
					} else // create new entry
					{
						DataField dField = insertSQL.getDbTable().getField(column);
						ThriftUtils.createSymbolEntry(txnContext, value, dField, insertSQL.getDbTable());
						insertSQL.addSymbolEntry(value, column);
						String tmpId = symbolsManager.createIdForSymbol(value);
						symbolsManager.linkRecordWithSymbol(insertSQL.getRecord(), value);
						value = tmpId;
					}
				}

				insertSQL.addRecordEntry(column, value);

				if(colIt.hasNext())
					insertSQL.prepareForNextInput();

				if(value.contains("SELECT") || value.contains("select"))
				{
					throw new JSQLParserException("nested select not yet supported");
						/*
						SQLOperation sqlOperation = SQLOperation.parseSQLOperation(value);

						if(sqlOperation.getOpType() == SQLOperationType.SELECT)
						{
							SQLSelect sqlSelect = (SQLSelect) sqlOperation;
							if(value.indexOf("(") == 0 && value.lastIndexOf(")") == value.length() - 1)
								value = value.substring(1, value.length() - 1);

							PlainSelect plainSelect = (PlainSelect) sqlSelect.getSelect().getSelectBody();
							int selectItemCount = plainSelect.getSelectItems().size();
							PreparedStatement sPst;
							ResultSet rs = null;
							try
							{
								sPst = con.prepareStatement(value);
								rs = sPst.executeQuery();
								if(rs.next())
								{
									for(int i = 0; i < selectItemCount; i++)
										valList.add(rs.getString(i + 1));

								} else
									throw new SQLException("nested SELECT in sql query must return a value");

							} catch(SQLException e)
							{
								e.printStackTrace();
							} finally
							{
								DbUtils.closeQuietly(rs);
							}


						}
						else
							throw new JSQLParserException("could not parse nested SELECT");
						*/
				}
			}

			if(insertSQL.isMissingValues())
			{
				Set<DataField> missing = insertSQL.getMissingFields();

				for(DataField dField : missing)
				{
					if(dField.getDefaultFieldValue() != null)
						insertSQL.addRecordEntry(dField.getFieldName(),
								dField.getDefaultFieldValue().getFormattedValue());
					else if(!dField.isAutoIncrement())
						throw new SQLException(
								"only auto_increment fields or that have a default value set can be " + "missing " +
										"from" +
										" " +
										"SQL query");
					else // is auto_increment
					{
						//TODO we must generate a different symbol to exchange in replicator
						insertSQL.addRecordEntry(dField.getFieldName(), SymbolsManager.ONE_TIME_SYMBOL);
					}
				}
			}

			if(!insertSQL.getDbTable().isFreeToInsert())
			{
				for(UniqueConstraint uniqueConstraint : insertSQL.getDbTable().getUniqueConstraints())
				{
					if(!uniqueConstraint.requiresCoordination())
						continue;

					List<DataField> fieldsList = uniqueConstraint.getFields();
					StringBuilder uniqueBuffer = new StringBuilder();

					for(DataField aField : fieldsList)
					{
						uniqueBuffer.append(insertSQL.getRecord().getData(aField.getFieldName()));
						uniqueBuffer.append("_");
					}

					uniqueBuffer.setLength(uniqueBuffer.length() - 1);
					UniqueValue uniqueRequest = new UniqueValue();
					uniqueRequest.setConstraintId(uniqueConstraint.getConstraintIdentifier());
					uniqueRequest.setValue(uniqueBuffer.toString());
					txnContext.getCoordinatorRequest().addToUniqueValues(uniqueRequest);
				}
			}

			SQLOperation[] insert = new SQLOperation[1];
			insert[0] = insertSQL;
			return insert;
		}
	}
}
