package database.execution.temporary;


import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import nodes.proxy.IProxyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import runtime.Transaction;
import util.ExitCode;
import util.defaults.ScratchpadDefaults;

import java.io.StringReader;
import java.sql.*;
import java.sql.Statement;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class ImprovedScratchpad implements Scratchpad
{

	private static final Logger LOG = LoggerFactory.getLogger(ImprovedScratchpad.class);

	private Transaction transaction;
	private int id;
	private Map<String, DBExecutorAgent> executers;
	private final CCJSqlParserManager parser;
	private Connection defaultConnection;
	private Statement statQ;
	private Statement statU;
	private Statement statBU;
	private boolean batchEmpty;

	public ImprovedScratchpad(int id, Connection dbConnection, CCJSqlParserManager parser) throws SQLException
	{
		this.id = id;
		this.defaultConnection = dbConnection;
		this.executers = new HashMap<>();
		this.batchEmpty = true;
		this.parser = parser;
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();

		this.createDBExecuters();
	}

	@Override
	public void startTransaction(Transaction txn) throws SQLException
	{

		this.resetScratchpad();

		this.transaction = txn;

		if(LOG.isTraceEnabled())
			LOG.trace("Beggining txn {} on sandbox {}", transaction.getTxnId(), this.id);
	}

	@Override
	public void commitTransaction(IProxyNetwork network) throws SQLException
	{
		RuntimeUtils.throwRunTimeException("missing method implementation", ExitCode.MISSING_IMPLEMENTATION);
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

			DBExecutorAgent executor = this.executers.get(tableName);

			if(executor == null)
			{
				LOG.error("executor for table {} not found", tableName);
				throw new SQLException("executor not found");
			} else
				return executor.executeTemporaryQueryOnSingleTable(selectStatement, this);
		} else
			//TODO: implement multi-table queries (joins)
			return executeQueryMainStorage(op);
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

		DBExecutorAgent agent = this.executers.get(tableName);

		if(agent == null)
		{
			LOG.error("executor agent for table {} not found", tableName);
			throw new SQLException("executor agent not found");
		} else
			return agent.executeTemporaryUpdate(statement, this);
	}

	@Override
	public ResultSet executeQueryMainStorage(String op) throws SQLException
	{
		return this.statQ.executeQuery(op);
	}

	@Override
	public int executeUpdateMainStorage(String op) throws SQLException
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
			LOG.warn("trying to execute an empty batch in scratchpad {}", this.getScratchpadId());
			// -1 for default error
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
	public Transaction getActiveTransaction()
	{
		return this.transaction;
	}

	private void createDBExecuters() throws SQLException
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
			DBExecutorAgent executor = new DBExecutorAgent(i, tableName);
			executor.setup(metadata, this);
			this.executers.put(tableName.toUpperCase(), executor);
			this.defaultConnection.commit();
		}
	}

	@Override
	public void resetScratchpad() throws SQLException
	{
		this.transaction = null;
		this.statBU.clearBatch();

		for(DBExecutorAgent agent : this.executers.values())
			agent.resetExecuter(this);

		this.executeBatch();

		this.batchEmpty = true;
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
