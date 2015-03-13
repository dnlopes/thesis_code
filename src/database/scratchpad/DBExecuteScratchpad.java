package database.scratchpad;

import database.invariants.ForeignKeyInvariant;
import database.invariants.Invariant;
import database.jdbc.ConnectionFactory;
import database.util.DataField;
import database.util.Database;
import database.util.DatabaseTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.defaults.ScratchpadDefaults;

import java.sql.*;
import java.util.*;


/**
 * Created by dnlopes on 10/03/15.
 */
public class DBExecuteScratchpad implements ExecuteScratchpad
{

	static final Logger LOG = LoggerFactory.getLogger(DBExecuteScratchpad.class);

	private boolean readOnly;
	private int id;
	private Connection conn;
	private Statement stat;

	public DBExecuteScratchpad(int id) throws SQLException, ScratchpadException
	{
		this.id = id;
		this.readOnly = false;
		this.conn = ConnectionFactory.getInstance().getDefaultConnection("micro");
		this.stat = this.conn.createStatement();
		this.initScratchpad();
		//this.init();
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
	public ResultSet executeQuery(String op) throws SQLException
	{
		//TODO
		return this.stat.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException
	{
		//TODO
		return this.stat.executeUpdate(op);
	}

	@Override
	public void addToBatchUpdate(String op) throws SQLException
	{
		//TODO
		this.stat.addBatch(op);
	}

	@Override
	public int executeBatch() throws SQLException
	{
		//TODO
		int[] results = this.stat.executeBatch();
		int finalResult = 0;

		for(int i = 0; i < results.length; i++)
			finalResult += results[i];

		return finalResult;
	}

	@Override
	public void cleanState()
	{

	}

	private void init() throws SQLException
	{
		Database db = Database.getInstance();
		Collection<DatabaseTable> allTables = db.getTables();

		for(DatabaseTable table : allTables)
		{
			this.deleteTable(table.getTableName());
			this.conn.commit();
			this.createTable(table);
			this.conn.commit();
		}
	}

	private void deleteTable(String name) throws SQLException
	{
		Statement statement = this.conn.createStatement();
		StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");

		sql.append(this.getTransformedTableName(name));
		sql.append(";");

		LOG.info("deleteTable(): executing sql: {}", sql);
		statement.execute(sql.toString());
	}

	private void createTable(DatabaseTable table) throws SQLException
	{
		StringBuilder createTableStatement = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
		createTableStatement.append(ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX);
		createTableStatement.append(this.id);
		createTableStatement.append("_");
		createTableStatement.append(table.getTableName());
		createTableStatement.append("(");

		// we scan all fields and get all invariants at the same time
		List<Invariant> invariants = new LinkedList<>();

		// create fields now
		HashMap<Integer, DataField> map = table.getSortedFieldsMap();

		for(DataField field : map.values())
		{
			createTableStatement.append(field.getOriginalDeclaration());
			createTableStatement.append(",");

			if(field.hasInvariants())
			{
				List<Invariant> fieldInvariants = field.getInvariants();
				for(Invariant inv : fieldInvariants)
					invariants.add(inv);
			}
		}

		// create constraints now
		for(Invariant inv : invariants)
		{
			if(inv instanceof ForeignKeyInvariant)
			{
				String originalTableName = ((ForeignKeyInvariant) inv).getReferenceTable();
				String origDeclaration = inv.getOriginalDeclaration();
				String transformedDecl = origDeclaration.replace(originalTableName,
						this.getTransformedTableName(originalTableName));
				createTableStatement.append(transformedDecl);
				createTableStatement.append(",");

			} else
			{
				createTableStatement.append(inv.getOriginalDeclaration());
				createTableStatement.append(",");
			}
		}

		char last = createTableStatement.charAt(createTableStatement.length() - 1);

		if(last == ',')
			createTableStatement = new StringBuilder(
					createTableStatement.substring(0, createTableStatement.length() - 1));

		createTableStatement.append(");");
		Statement statement = this.conn.createStatement();
		String sql = createTableStatement.toString();

		LOG.info("createTable(): executing sql: {}", sql);
		statement.execute(sql);
	}

	private void initScratchpad() throws SQLException, ScratchpadException
	{
		DatabaseMetaData metadata = this.conn.getMetaData();
		String[] types = {"TABLE"};
		ResultSet tblSet = metadata.getTables(null, null, "%", types);

		ArrayList<String> tables = new ArrayList<>();
		while(tblSet.next())
		{
			String tableName = tblSet.getString(3);
			if(tableName.startsWith(ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX))
				continue;
			tables.add(tableName);
		}
		Collections.sort(tables);

		for(int i = 0; i < tables.size(); i++)
		{
			String tableName = tables.get(i);
			this.createTempTable(metadata, tableName, this.id);
			this.conn.commit();
		}
	}

	private void createTempTable(DatabaseMetaData metadata, String tableName, int id) throws ScratchpadException
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
				if(! first)
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
				//System.err.println("INFO scratchpad: read column:"+tmpStr[0]+" sql:"+buffer);
				buffer.append(tmpStr[0]);
				if(! (tmpStr[0].equals("INT") ||
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


			// start foreign key now
			ResultSet foreignSet = metadata.getExportedKeys(null, null, tableName);

			first = true;

			while(foreignSet.next())
			{
				if(first)
				{
					buffer.append(", FOREIGN KEY (");
					first = false;
				}
				else
					buffer.append(", ");

				//String PKTABLE_CAT = foreignSet.getString(1);
				//String PKTABLE_SCHEM = foreignSet.getString(2);
				String PKTABLE_NAME = foreignSet.getString(3); // references table
				String PKCOLUMN_NAME = foreignSet.getString(4); //referenced attribute
				//String FKTABLE_CAT = foreignSet.getString(5);
				//String FKTABLE_SCHEM = foreignSet.getString(6);
				String FKTABLE_NAME = foreignSet.getString(7);  // source table
				String FKCOLUMN_NAME = foreignSet.getString(8); // source attribute
				String a = "";

			}

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

			//			if( temp.size() != 1)
			//				throw new RuntimeException( "Does not support table with more than one primary key column : " + tableName + ":" + temp);

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
		StringBuilder transformed = new StringBuilder(ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX);
		transformed.append(this.id);
		transformed.append("_");
		transformed.append(tableName);

		return transformed.toString();
	}
}
