package client.proxy;


import applications.util.SymbolsManager;
import client.execution.CRDTOperationGenerator;
import client.execution.TransactionContext;
import client.execution.operation.*;
import client.proxy.network.WTIProxyNetwork;
import client.proxy.network.WTProxyNetwork;
import common.database.Record;
import common.database.SQLBasicInterface;
import common.database.SQLInterface;
import common.database.constraints.unique.AutoIncrementConstraint;
import common.database.field.DataField;
import common.database.util.DatabaseCommon;
import common.nodes.NodeConfig;
import common.thrift.RequestValue;
import common.util.ConnectionFactory;
import common.util.ExitCode;
import common.util.RuntimeUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by dnlopes on 07/12/15.
 */
public class WriteThroughProxy implements Proxy
{

	private static final Logger LOG = LoggerFactory.getLogger(SandboxProxy.class);

	private final int proxyId;
	private final WTIProxyNetwork network;
	private SQLInterface sqlInterface;

	private TransactionContext txnRecord;
	private boolean readOnly, isRunning;
	private SQLDeterministicTransformer transformer;
	private Map<String, String> symbolsMapping;
	private String clock;

	public WriteThroughProxy(final NodeConfig proxyConfig, int proxyId)
	{
		this.proxyId = proxyId;
		this.network = new WTProxyNetwork(proxyConfig);

		this.symbolsMapping = new HashMap<>();
		this.readOnly = false;
		this.isRunning = false;
		this.transformer = new SQLDeterministicTransformer();

		try
		{
			this.sqlInterface = new SQLBasicInterface(ConnectionFactory.getDefaultConnection(proxyConfig));
			this.txnRecord = new TransactionContext(sqlInterface);
		} catch(SQLException e)
		{
			LOG.error("failed to create connection", e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.SANDBOX_INIT_FAILED);
		}
	}

	@Override
	public int getProxyId()
	{
		return 0;
	}

	@Override
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
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
	public void commit() throws SQLException
	{
		long start = System.nanoTime();
		this.txnRecord.setExecTime(start - txnRecord.getStartTime());

		// if read-only, just return
		if(readOnly)
		{
			end();
			return;
		}

		try
		{
			this.sqlInterface.commit();
			this.network.sendToRemoteReplicators(this.txnRecord);
			long estimated = System.nanoTime() - start;
			this.txnRecord.setCommitTime(estimated);
		} catch(SQLException e)
		{
			throw new SQLException(e.getMessage());
		} finally
		{
			end();
		}
	}

	@Override
	public void close() throws SQLException
	{
		commit();
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		long start = System.nanoTime();

		//its the first op from this txn
		if(!isRunning)
			start();

		SQLOperation sqlOp;
		try
		{
			sqlOp = SQLOperation.parseSQLOperation(op);
		} catch(JSQLParserException e)
		{
			throw new SQLException(e.getMessage());
		}

		if(sqlOp.getOpType() != SQLOperationType.SELECT)
			throw new SQLException("expected query op but instead we got an update");

		SQLSelect selectSQL = (SQLSelect) sqlOp;
		selectSQL.prepareOperation();

		ResultSet rs = this.sqlInterface.executeQuery(selectSQL.getSQLString());

		long estimated = System.nanoTime() - start;
		this.txnRecord.addSelectTime(estimated);

		return rs;
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		if(this.readOnly)
			throw new SQLException("update statement not acceptable under read-only mode");

		//its the first op from this txn
		if(!this.isRunning)
			start();

		SQLOperation[] preparedOps;

		try
		{
			long start = System.nanoTime();
			preparedOps = this.transformer.pepareOperation(op);
			long estimated = System.nanoTime() - start;
			this.txnRecord.addToParsingTime(estimated);

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser exception");
		}

		int result = 0;

		for(SQLOperation updateOp : preparedOps)
		{
			if(updateOp.getOpType() == SQLOperationType.INSERT)
				result += executeInsert((SQLInsert) updateOp);
			else if(updateOp.getOpType() == SQLOperationType.DELETE)
				result += executeDelete((SQLDelete) updateOp);
			else if(updateOp.getOpType() == SQLOperationType.UPDATE)
				result += executeUpdate((SQLUpdate) updateOp);
			else
				throw new SQLException("unexpected sql statement");
		}

		return result;
	}

	private int executeInsert(SQLInsert sqlInsert) throws SQLException
	{
		long start = System.nanoTime();

		String[] crdtInserts;

		if(sqlInsert.getDbTable().getFkConstraints().size() > 0)
			crdtInserts = CRDTOperationGenerator.insertChildRow(sqlInsert.getRecord(), this.clock, null);
		else
			crdtInserts = CRDTOperationGenerator.insertRow(sqlInsert.getRecord(), this.clock, null);

		int result = 0;

		for(String crdtInsert : crdtInserts)
			result += this.sqlInterface.executeUpdate(crdtInsert);

		long estimated = System.nanoTime() - start;
		this.txnRecord.addInsertTime(estimated);
		this.txnRecord.addCrdtOp(crdtInserts);

		return result;
	}

	private int executeUpdate(SQLUpdate sqlUpdate) throws SQLException
	{
		long start = System.nanoTime();

		String[] crdtUpdates;

		if(sqlUpdate.getDbTable().getFkConstraints().size() > 0)
			crdtUpdates = CRDTOperationGenerator.updateChildRow(sqlUpdate.getRecord(), this.clock, null);
		else
			crdtUpdates = CRDTOperationGenerator.updateRow(sqlUpdate.getRecord(), this.clock, null);

		int result = 0;

		for(String crdtUpdate : crdtUpdates)
			result += this.sqlInterface.executeUpdate(crdtUpdate);

		long estimated = System.nanoTime() - start;
		this.txnRecord.addUpdateTime(estimated);
		this.txnRecord.addCrdtOp(crdtUpdates);

		return result;
	}

	private int executeDelete(SQLDelete sqlDelete) throws SQLException
	{
		long start = System.nanoTime();

		String[] crdtDeletes;

		if(sqlDelete.getDbTable().isParentTable())
			crdtDeletes = CRDTOperationGenerator.deleteParentRow(sqlDelete.getRecord(), this.clock, null);
		else
			crdtDeletes = CRDTOperationGenerator.deleteRow(sqlDelete.getRecord(), this.clock, null);

		int result = 0;

		for(String crdtDelete : crdtDeletes)
			result += this.sqlInterface.executeUpdate(crdtDelete);

		long estimated = System.nanoTime() - start;
		this.txnRecord.addDeleteTime(estimated);
		this.txnRecord.addCrdtOp(crdtDeletes);

		return result;
	}

	private void start()
	{
		reset();
		txnRecord.setStartTime(System.nanoTime());
		this.clock = this.network.getTransactionClock();
		this.isRunning = true;
	}

	private void end()
	{
		txnRecord.setEndTime(System.nanoTime());
		this.isRunning = false;
	}

	private void reset()
	{
		clock = null;
		txnRecord.clear();
		symbolsMapping.clear();
	}

	public final class SQLDeterministicTransformer
	{

		private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

		public SQLOperation[] pepareOperation(String sqlOpString) throws JSQLParserException
		{
			SQLOperation sqlOp = SQLOperation.parseSQLOperation(sqlOpString);

			if(sqlOp.getOpType() == SQLOperationType.INSERT)
				return prepareInsertOperation((SQLInsert) sqlOp);
			else if(sqlOp.getOpType() == SQLOperationType.UPDATE)
				return prepareUpdateOperation((SQLUpdate) sqlOp);
			else if(sqlOp.getOpType() == SQLOperationType.DELETE)
				return prepareDeleteOperation((SQLDelete) sqlOp);
			else
				throw new JSQLParserException("unkown SQL operation type");

		}

		private SQLOperation[] prepareDeleteOperation(SQLDelete deleteSQL) throws JSQLParserException
		{
			if(deleteSQL.isPrimaryKeyMissingFromWhere())
			{
				//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
				//it not, please first query
				StringBuilder sqlQuery = new StringBuilder("SELECT ");
				sqlQuery.append(deleteSQL.getPk().getQueryClause());
				sqlQuery.append(" FROM ").append(deleteSQL.getDbTable().getName());
				sqlQuery.append(" WHERE ").append(deleteSQL.getDelete().getWhere().toString());
				PreparedStatement sPst;
				List<SQLOperation> deleteOps = new LinkedList<>();

				try
				{
					sPst = sqlInterface.prepareStatement(sqlQuery.toString());
					ResultSet rs = sPst.executeQuery();

					while(rs.next())
					{
						SQLDelete aDelete = deleteSQL.duplicate();
						Record toDeleteRecord = aDelete.getRecord();

						for(DataField pkField : deleteSQL.getPk().getPrimaryKeyFields().values())
						{
							String value = rs.getString(pkField.getFieldName()).trim();
							toDeleteRecord.addData(pkField.getFieldName(), value);
						}

						if(!toDeleteRecord.isPrimaryKeyReady())
							throw new SQLException("failed to retrieve pk value from main storage");

						deleteOps.add(aDelete);
					}

				} catch(SQLException e)
				{
					throw new JSQLParserException("failed to retrieve pks values for non-deterministic update");
				}

				return deleteOps.toArray(new SQLOperation[deleteOps.size()]);
			} else
			{
				//TODO implement simple case delete
				int a = 0;
				return null;
				//deterQueries = fillInMissingPrimaryKeysForDelete(con, deleteStmt);
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

				if(value.contains("+"))
				{
					//TODO verify if check constraint must be coordinated
				} else if(value.contains("-"))
				{
					//TODO verify if check constraint must be coordinated
				}

				if(value.equalsIgnoreCase("NOW()") || value.equalsIgnoreCase("NOW") || value.equalsIgnoreCase(
						"CURRENT_TIMESTAMP") || value.equalsIgnoreCase("CURRENT_TIMESTAMP()") || value
						.equalsIgnoreCase("CURRENT_DATE"))
					value = "'" + DatabaseCommon.CURRENTTIMESTAMP(DATE_FORMAT) + "'";

				if(value.contains("SELECT") || value.contains("select"))
					throw new JSQLParserException("nested select not yet supported");

				updateSQL.addRecordValue(column, value);

				if(colIt.hasNext())
					updateSQL.prepareForNextInput();
			}

			long start = System.nanoTime();

			//load whole record from database
			StringBuilder sqlQuery = new StringBuilder("SELECT ");
			sqlQuery.append(updateSQL.getDbTable().getNormalFieldsSelection());
			sqlQuery.append(" FROM ").append(updateSQL.getDbTable().getName());
			sqlQuery.append(" WHERE ").append(updateSQL.getUpdate().getWhere().toString());
			List<SQLOperation> updateOps = new LinkedList<>();

			try
			{
				ResultSet rs = sqlInterface.executeQuery(sqlQuery.toString());

				while(rs.next())
				{
					SQLUpdate anUpdate = updateSQL.duplicate();
					Record cachedRecord = DatabaseCommon.loadRecordFromResultSet(rs, anUpdate.getDbTable());

					if(!cachedRecord.isFullyCached())
						throw new SQLException("failed to retrieve full row from main storage for record update");

					anUpdate.setCachedRecord(cachedRecord);
					updateOps.add(anUpdate);
				}

			} catch(SQLException e)
			{
				throw new JSQLParserException("failed to retrieve pks values for non-deterministic update");
			}

			long estimated = System.nanoTime() - start;
			txnRecord.addLoadfromMainTime(estimated);

			return updateOps.toArray(new SQLOperation[updateOps.size()]);
		}

		private SQLOperation[] prepareInsertOperation(SQLInsert insertSQL) throws JSQLParserException
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
					if(symbolsMapping.containsKey(value)) // get tmp value already generated
					{
						value = symbolsMapping.get(value);
					} else // create new entry
					{
						RequestValue requestValue = new RequestValue();
						AutoIncrementConstraint autoIncrementConstraint = insertSQL.getDbTable()
								.getAutoIncrementConstraint(column);
						requestValue.setConstraintId(autoIncrementConstraint.getConstraintIdentifier());

						int generatedId = network.requestNextId(requestValue);
						String idString = String.valueOf(generatedId);
						symbolsMapping.put(value, idString);
						value = idString;
					}
				}

				insertSQL.addRecordValue(column, value);

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
						insertSQL.addRecordValue(dField.getFieldName(),
								dField.getDefaultFieldValue().getFormattedValue());
					else if(!dField.isAutoIncrement())
						RuntimeUtils.throwRunTimeException(
								"missing a column value which does not have a default value and is not an " +
										"auto_increment field",
								ExitCode.ERRORTRANSFORM);
					else // is auto_increment
					{
						RequestValue requestValue = new RequestValue();
						AutoIncrementConstraint autoIncrementConstraint = insertSQL.getDbTable()
								.getAutoIncrementConstraint(dField.getFieldName());
						requestValue.setConstraintId(autoIncrementConstraint.getConstraintIdentifier());

						int generatedId = network.requestNextId(requestValue);
						String idString = String.valueOf(generatedId);
						insertSQL.prepareForNextInput();
						insertSQL.addRecordValue(dField.getFieldName(), idString);
					}
				}
			}

			SQLOperation[] insert = new SQLOperation[1];
			insert[0] = insertSQL;
			return insert;
		}
	}
}
