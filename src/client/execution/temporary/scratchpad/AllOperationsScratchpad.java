package client.execution.temporary.scratchpad;


import common.database.SQLInterface;
import client.execution.temporary.DBExecutorAgent;
import client.execution.temporary.IExecutorAgent;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.defaults.ScratchpadDefaults;
import common.thrift.CRDTTransaction;

import java.io.StringReader;
import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 25/09/15.
 */
public class AllOperationsScratchpad implements ReadWriteScratchpad
{

	private static final Logger LOG = LoggerFactory.getLogger(AllOperationsScratchpad.class);

	private CRDTTransaction transaction;
	private int id;
	private Map<String, IExecutorAgent> executers;
	private final CCJSqlParserManager parser;
	private SQLInterface dbInterface;

	public AllOperationsScratchpad(int id, SQLInterface sqlInterface, CCJSqlParserManager parser) throws SQLException
	{
		this.id = id;
		this.executers = new HashMap<>();
		this.parser = parser;
		this.dbInterface = sqlInterface;

		this.createDBExecuters();
	}

	@Override
	public void startTransaction(CRDTTransaction txn) throws SQLException
	{
		this.resetScratchpad();

		this.transaction = txn;

		if(LOG.isTraceEnabled())
			LOG.trace("Beggining txn {} on sandbox {}", transaction.getId(), this.id);
	}

	@Override
	public int getScratchpadId()
	{
		return this.id;
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		net.sf.jsqlparser.statement.Statement statement;

		try
		{
			statement = this.parser.parse(new StringReader(op));

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(!(statement instanceof Select))
			throw new SQLException("query statement expected");

		Select selectStatement = (Select) statement;

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
			return dbInterface.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		net.sf.jsqlparser.statement.Statement statement;

		try
		{
			statement = this.parser.parse(new StringReader(op));

		} catch(JSQLParserException e)
		{
			throw new SQLException("parser error: " + e.getMessage());
		}

		if(statement instanceof Select)
			throw new SQLException("update operation expected but instead we got a select");

		String tableName = this.getTableName(statement);

		IExecutorAgent agent = this.executers.get(tableName);

		if(agent == null)
		{
			LOG.error("executor agent for table {} not found", tableName);
			throw new SQLException("executor agent not found");
		} else
			return agent.executeTemporaryUpdate(statement, this.transaction);
	}

	@Override
	public CRDTTransaction getActiveTransaction()
	{
		return this.transaction;
	}

	private void createDBExecuters() throws SQLException
	{
		DatabaseMetaData metadata = this.dbInterface.getConnection().getMetaData();
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
			DBExecutorAgent executor = new DBExecutorAgent(this.id, i, tableName, this.dbInterface);
			executor.setup(metadata, this.id);
			this.executers.put(tableName.toUpperCase(), executor);
			this.dbInterface.commit();
		}
	}

	@Override
	public void resetScratchpad() throws SQLException
	{
		this.transaction = null;

		for(IExecutorAgent agent : this.executers.values())
			agent.resetExecuter();
	}

	private String getTableName(net.sf.jsqlparser.statement.Statement statement) throws SQLException
	{
		if(statement instanceof Insert)
			return ((Insert) statement).getTable().getName().toUpperCase();
		if(statement instanceof Delete)
			return ((Delete) statement).getTable().getName().toUpperCase();
		if(statement instanceof Update)
		{
			if(((Update) statement).getTables().size() != 1)
				throw new SQLException("multi-table update operation not supported");
			return ((Update) statement).getTables().get(0).getName().toUpperCase();
		} else
			throw new SQLException("could not parse statement table name");
	}

}
