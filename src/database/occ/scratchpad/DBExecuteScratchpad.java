package database.occ.scratchpad;

import database.jdbc.ConnectionFactory;
import database.jdbc.Result;
import database.occ.OCCExecuter;
import database.occ.IExecutor;
import database.jdbc.util.DBReadSetEntry;
import database.jdbc.util.DBWriteSetEntry;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.*;
import runtime.Runtime;
import runtime.operation.DBSingleOperation;
import util.ExitCode;
import util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchpad implements IDBScratchpad
{

	private static final Logger LOG = LoggerFactory.getLogger(DBExecuteScratchpad.class);

	private Map<String, IExecutor> executors;
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
		this.parser = new CCJSqlParserManager();
		this.defaultConnection = ConnectionFactory.getInstance().getDefaultConnection(Configuration.DB_NAME);
		this.executors = new HashMap<>();
		this.readOnly = false;
		this.batchEmpty = true;
		this.statQ = this.defaultConnection.createStatement();
		this.statU = this.defaultConnection.createStatement();
		this.statBU = this.defaultConnection.createStatement();
		this.createExecutors();
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
	public Result execute(DBSingleOperation op)
			throws JSQLParserException, ScratchpadException, SQLException
	{
		op.parseSQL(parser);

		String[][] tableName = op.targetTable();
		if(tableName.length == 1)
		{
			IExecutor executor = this.executors.get(tableName[0][2]);
			if(executor == null)
			{
				LOG.error("executor for table {} not found", tableName[0][2]);
				Runtime.throwRunTimeException("could not find a proper executor for this operation",
						ExitCode.EXECUTOR_NOT_FOUND);
			}
			if(op.isQuery())
				return executor.executeTemporaryQuery(op, this, tableName[0]);
			else
				return executor.executeTemporaryUpdate(op, this);
		} else
		{
			if(!op.isQuery())
				throw new ScratchpadException("Multi-table operation not expected " + op.sql);
			IExecutor[] policy = new OCCExecuter[tableName.length];
			for(int i = 0; i < tableName.length; i++)
			{
				policy[i] = this.executors.get(tableName[i][2]);
				if(tableName[i][1] == null)
					tableName[i][1] = policy[i].getAliasTable();
				if(policy[i] == null)
					throw new ScratchpadException("No config for table " + tableName[i][0]);
			}
			return policy[0].executeTemporaryQuery(op, this, policy, tableName);
		}
		//TODO
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException
	{
		//TODO
		return this.statQ.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		//TODO
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
			LOG.warn("trying to execute an empty batch for pad {}", this.getScratchpadId());
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

	private void createExecutors() throws SQLException, ScratchpadException
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
			IExecutor executor = new OCCExecuter(true);
			executor.setup(metadata, tableName, i, this);
			this.executors.put(tableName.toUpperCase(), executor);

			//this.createTempTable(metadata, tableName);
			this.defaultConnection.commit();
		}
	}

	private void createTempTable(DatabaseMetaData metadata, String tableName) throws ScratchpadException
	{
		try
		{
			String tempTableName = ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + tableName + "_" + id;
			String tempTableNameAlias = ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + this.id;
			String tableNameAlias = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.id;
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("DROP TABLE IF EXISTS ");
			StringBuffer buffer = new StringBuffer();
			buffer.append("CREATE TABLE IF NOT EXISTS ");
			buffer.append(tempTableName);
			buffer2.append(tempTableName);
			buffer2.append(";");
			buffer.append("(");
			ArrayList<Boolean> tempIsStr = new ArrayList<>();        // for columns
			ArrayList<String> temp = new ArrayList<>();        // for columns
			ArrayList<String> tempAlias = new ArrayList<>();    // for columns with aliases
			ArrayList<String> tempTempAlias = new ArrayList<>();    // for temp columns with aliases
			ArrayList<String> uniqueIndices = new ArrayList<>(); // unique index
			ResultSet colSet = metadata.getColumns(null, null, tableName, "%");
			boolean first = true;
			while(colSet.next())
			{
				if(!first)
					buffer.append(",");
				else
					first = false;
				buffer.append(colSet.getString(4));            // column name
				buffer.append(" ");
				String[] tmpStr = {""};
				if(colSet.getString(6).contains(" "))
				{        // column type
					tmpStr = colSet.getString(6).split(" ");
				} else
				{
					tmpStr[0] = colSet.getString(6);
				}

				buffer.append(tmpStr[0]);
				if(!(tmpStr[0].equals("INT") ||
						tmpStr[0].equals("DOUBLE") ||
						tmpStr[0].equals("BIT") ||
						tmpStr[0].equals("DATE") ||
						tmpStr[0].equals("TIME") ||
						tmpStr[0].equals("TIMESTAMP") ||
						tmpStr[0].equals("DATETIME") ||
						tmpStr[0].equals("YEAR")))
				{
					buffer.append("(");
					buffer.append(colSet.getInt(7));        //size of type
					buffer.append(")");
				}
				buffer.append(" ");
				if(tmpStr.length > 1)
					buffer.append(tmpStr[1]);
				if(colSet.getString(4).equalsIgnoreCase(ScratchpadDefaults.SCRATCHPAD_COL_DELETED))
				{
					buffer.append(" DEFAULT FALSE ");
				}
				temp.add(colSet.getString(4));
				tempAlias.add(tableNameAlias + "." + colSet.getString(4));
				tempTempAlias.add(tempTableNameAlias + "." + colSet.getString(4));
				tempIsStr.add(colSet.getInt(5) == java.sql.Types.VARCHAR || colSet.getInt(
						5) == java.sql.Types.LONGNVARCHAR || colSet.getInt(
						5) == java.sql.Types.LONGVARCHAR || colSet.getInt(5) == java.sql.Types.CHAR || colSet.getInt(
						5) == java.sql.Types.DATE || colSet.getInt(5) == java.sql.Types.TIMESTAMP || colSet.getInt(
						5) == java.sql.Types.TIME);
			}
			colSet.close();
			String[] cols = new String[temp.size()];
			temp.toArray(cols);
			temp.clear();

			String[] aliasCols = new String[tempAlias.size()];
			tempAlias.toArray(aliasCols);
			tempAlias.clear();

			String[] tempAliasCols = new String[tempTempAlias.size()];
			tempTempAlias.toArray(tempAliasCols);
			tempTempAlias.clear();

			boolean[] colsIsStr = new boolean[tempIsStr.size()];
			for(int i = 0; i < colsIsStr.length; i++)
				colsIsStr[i] = tempIsStr.get(i);

			//get all unique index
			ResultSet uqIndices = metadata.getIndexInfo(null, null, tableName, true, true);
			while(uqIndices.next())
			{
				String indexName = uqIndices.getString("INDEX_NAME");
				String columnName = uqIndices.getString("COLUMN_NAME");
				if(indexName == null)
				{
					continue;
				}
				uniqueIndices.add(columnName);
			}
			uqIndices.close();

			ResultSet pkSet = metadata.getPrimaryKeys(null, null, tableName);
			while(pkSet.next())
			{
				if(temp.size() == 0)
					buffer.append(", PRIMARY KEY (");
				else
					buffer.append(", ");
				buffer.append(pkSet.getString(4));
				temp.add(pkSet.getString(4));
				tempAlias.add(tableNameAlias + "." + pkSet.getString(4));
				tempTempAlias.add(tempTableNameAlias + "." + pkSet.getString(4));
				uniqueIndices.remove(pkSet.getString(4));
			}
			pkSet.close();

			if(temp.size() > 0)
				buffer.append(")");

			String[] pkPlain = new String[temp.size()];
			temp.toArray(pkPlain);
			temp.clear();

			String[] pkAlias = new String[tempAlias.size()];
			tempAlias.toArray(pkAlias);
			tempAlias.clear();

			String[] pkTempAlias = new String[tempTempAlias.size()];
			tempTempAlias.toArray(pkTempAlias);
			tempTempAlias.clear();

			String[] uqIndicesPlain = new String[uniqueIndices.size()];
			uniqueIndices.toArray(uqIndicesPlain);
			uniqueIndices.clear();

			buffer.append(") ENGINE=MEMORY;");    // FOR MYSQL

			this.executeUpdate(buffer2.toString());
			this.executeUpdate(buffer.toString());

		} catch(SQLException e)
		{
			throw new ScratchpadException(e);
		}
	}

	private String getTransformedTableName(String tableName)
	{
		StringBuilder transformed = new StringBuilder(ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX);
		transformed.append(tableName);
		transformed.append("_");
		transformed.append(this.id);

		return transformed.toString();
	}
}
