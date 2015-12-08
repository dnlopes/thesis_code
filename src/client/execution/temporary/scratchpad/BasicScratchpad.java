package client.execution.temporary.scratchpad;


import client.execution.TransactionContext;
import client.execution.operation.*;
import client.execution.temporary.scratchpad.agent.DBExecutorPerfAgent;
import common.database.SQLInterface;
import client.execution.temporary.scratchpad.agent.IExecutorAgent;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 25/09/15.
 */
public class BasicScratchpad implements ReadWriteScratchpad
{

	private static final Logger LOG = LoggerFactory.getLogger(BasicScratchpad.class);

	private int scratchpadId;
	private Map<String, IExecutorAgent> executers;
	private SQLInterface sqlInterface;
	private TransactionContext txnRecord;

	public BasicScratchpad(SQLInterface sqlInterface, TransactionContext txnRecord) throws SQLException
	{
		this.executers = new HashMap<>();
		this.sqlInterface = sqlInterface;
		this.txnRecord = txnRecord;
		assignScratchpadId();
		createDBExecuters();
	}

	@Override
	public ResultSet executeQuery(SQLSelect sqlSelect) throws SQLException
	{
		Select selectStatement = sqlSelect.getSelect();

		SelectBody sb = selectStatement.getSelectBody();

		if(!(sb instanceof PlainSelect))
			throw new SQLException("Cannot process select : " + selectStatement.toString());

		PlainSelect psb = (PlainSelect) sb;
		FromItem fi = psb.getFromItem();

		if(!(fi instanceof Table))
			throw new RuntimeException("Cannot process select : " + selectStatement.toString());

		List joins = psb.getJoins();
		int nJoins = joins == null ? 0 : joins.size();

		String tableName;
		if(nJoins == 0)
		{
			tableName = ((Table) fi).getName().toUpperCase();

			IExecutorAgent executor = this.executers.get(tableName);

			if(executor == null)
			{
				LOG.error("executor for table {} not found", tableName);
				throw new SQLException("executor not found");
			} else
				return executor.executeTemporaryQuery(selectStatement);
		} else
		//TODO: implement multi-table queries (joins)
		{
			LOG.warn("multi-table queries not yet implemented");
			return sqlInterface.executeQuery(selectStatement.toString());
		}
	}

	@Override
	public int executeUpdate(SQLWriteOperation sqlWriteOp) throws SQLException
	{
		if(sqlWriteOp.getOpType() == SQLOperationType.SELECT)
			throw new SQLException("update operation expected but instead we got a select query");

		IExecutorAgent agent = this.executers.get(sqlWriteOp.getDbTable().getName().toUpperCase());

		if(agent == null)
		{
			LOG.error("executor agent for table {} not found", sqlWriteOp.getDbTable().getName());
			throw new SQLException("executor agent not found");
		} else
		{
			return agent.executeTemporaryUpdate(sqlWriteOp);
		}
	}

	@Override
	public void clearScratchpad() throws SQLException
	{
		for(IExecutorAgent agent : this.executers.values())
			agent.clearExecutor();
	}

	private void createDBExecuters() throws SQLException
	{
		DatabaseMetaData metadata = sqlInterface.getConnection().getMetaData();
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
			//IExecutorAgent executor = new DBExecutorAgentPassBy(this.id, i, tableName, this.dbInterface. this);
			IExecutorAgent executor = new DBExecutorPerfAgent(scratchpadId, i, tableName, this.sqlInterface, this,
					txnRecord);
			//IExecutorAgent executor = new DBExecutorAgent(this.id, i, tableName, this.dbInterface, this, txnRecord);
			executor.setup(metadata, scratchpadId);
			this.sqlInterface.commit();
			this.executers.put(tableName.toUpperCase(), executor);
		}
	}

	private void assignScratchpadId()
	{
		createScratchpadIdsTable();

		for(; ; )
		{
			ResultSet rs = null;
			try
			{
				rs = sqlInterface.executeQuery("SELECT id FROM " + ScratchpadDefaults.SCRATCHPAD_IDS_TABLE + " WHERE" +
						" k = 1");

				rs.next();
				int id = rs.getInt(1);
				sqlInterface.executeUpdate(
						"UPDATE " + ScratchpadDefaults.SCRATCHPAD_IDS_TABLE + " SET id = id + 1 WHERE" +
								" " +
								"k = 1;");
				sqlInterface.commit();
				scratchpadId = id;
				return;
			} catch(SQLException e)
			{
				LOG.trace(e.getMessage());
			} finally
			{
				DbUtils.closeQuietly(rs);
			}
		}
	}

	private void createScratchpadIdsTable()
	{
		try
		{
			this.sqlInterface.executeUpdate("CREATE TABLE " + ScratchpadDefaults.SCRATCHPAD_IDS_TABLE + " ( k int " +
					"NOT" +
					" " +
					"NULL primary key, id " +
					"int)");
			this.sqlInterface.commit();
			this.sqlInterface.executeUpdate("INSERT INTO " + ScratchpadDefaults.SCRATCHPAD_IDS_TABLE + " VALUES (1," +
					"1)");
			this.sqlInterface.commit();

		} catch(SQLException e)
		{
			LOG.trace("scratchpad_id table already exists");
		}

	}

}
