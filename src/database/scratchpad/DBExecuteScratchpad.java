package database.scratchpad;


import database.jdbc.ConnectionFactory;
import database.jdbc.Result;
import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.*;
import runtime.RuntimeHelper;
import runtime.operation.DBSingleOperation;
import util.ExitCode;
import util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.sql.Statement;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchpad implements IDBScratchpad
{

	private static final Logger LOG = LoggerFactory.getLogger(DBExecuteScratchpad.class);

	private Map<String, IExecuter> executers;
	private boolean readOnly;
	private int id;
	private Connection defaultConnection;
	private Statement statQ;
	private Statement statU;
	private Statement statBU;
	private boolean batchEmpty;
	private CCJSqlParserManager parser;

	public DBExecuteScratchpad(int id) throws SQLException, ScratchpadException
	{
		this.id = id;
		this.defaultConnection = ConnectionFactory.getDefaultConnection(Configuration.getInstance().getDatabaseName());
		this.executers = new HashMap<>();
		this.readOnly = false;
		this.batchEmpty = true;
		this.parser = new CCJSqlParserManager();
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();
		this.createDBExecuters();
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

	@Override
	public Result executeUpdate(DBSingleOperation op) throws JSQLParserException, ScratchpadException, SQLException
	{
		op.parse(this.parser);

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
		//TODO
		LOG.debug("executing operation: {}", op);
		return this.statQ.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		//TODO
		LOG.debug("executing operation: {}", op);
		return this.statU.executeUpdate(op);
	}

	@Override
	public void addToBatchUpdate(String op) throws SQLException
	{
		this.statBU.addBatch(op);
		LOG.debug("executing operation: {}", op);
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
		LOG.trace("cleaning scratchpad {}", this.id);
		this.defaultConnection.rollback();
		LOG.trace("scratchpad {} cleaned", this.id);
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

	@Override
	public void resetScratchpad() throws SQLException
	{
		for(IExecuter executer : this.executers.values())
			executer.resetExecuter(this);

		this.executeBatch();
	}

	@Override
	public TransactionWriteSet createTransactionWriteSet() throws SQLException
	{
		TransactionWriteSet writeSet = new TransactionWriteSet();

		for(IExecuter executer: this.executers.values())
			writeSet.addTableWriteSet(executer.getTableName(), executer.createWriteSet(this));

		return writeSet;
	}

}
