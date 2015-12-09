package client.execution.temporary.scratchpad.agent;


import client.execution.TransactionContext;
import client.execution.temporary.TableDefinition;
import client.execution.temporary.scratchpad.ReadWriteScratchpad;
import client.execution.temporary.scratchpad.ScratchpadException;
import common.database.Record;
import common.database.SQLInterface;
import common.database.constraints.fk.ForeignKeyConstraint;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.database.table.TablePolicy;
import common.database.util.PrimaryKey;
import common.util.Environment;
import common.util.defaults.DatabaseDefaults;
import common.util.defaults.ScratchpadDefaults;
import common.util.exception.NotCallableException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 05/12/15.
 */
public abstract class AbstractExecAgent implements IExecutorAgent
{

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractExecAgent.class);

	protected String INSERT_PREFIX;
	protected SQLInterface sqlInterface;
	protected DatabaseTable databaseTable;
	protected int scratchpadId;
	protected String tempTableName;
	protected FromItem fromItemTemp;
	protected String tempTableNameAlias;
	protected int tableId;
	protected TableDefinition tableDefinition;
	protected TablePolicy tablePolicy;
	protected PrimaryKey pk;
	protected Map<String, DataField> fields;
	protected List<SelectItem> selectAllItems;
	protected List<ForeignKeyConstraint> fkConstraints;
	protected ReadWriteScratchpad scratchpad;
	protected TransactionContext txnRecord;
	protected boolean isDirty;

	public AbstractExecAgent(int scratchpadId, int tableId, String tableName, SQLInterface sqlInterface,
							 ReadWriteScratchpad scratchpad, TransactionContext txnRecord)
	{
		this.scratchpadId = scratchpadId;
		this.tableId = tableId;
		this.txnRecord = txnRecord;
		this.databaseTable = Environment.DB_METADATA.getTable(tableName);
		this.tablePolicy = this.databaseTable.getTablePolicy();
		this.pk = databaseTable.getPrimaryKey();
		this.sqlInterface = sqlInterface;
		this.fkConstraints = new ArrayList<>();
		this.fields = new HashMap<>();
		this.selectAllItems = new ArrayList<>();
		this.scratchpad = scratchpad;
		this.isDirty = false;

		for(DataField field : this.databaseTable.getFieldsMap().values())
		{
			if(field.isHiddenField())
				continue;
			this.fields.put(field.getFieldName(), field);
		}

		for(DataField field : this.fields.values())
		{
			if(field.isHiddenField())
				continue;

			Column column = new Column(field.getFieldName());
			SelectExpressionItem a = new SelectExpressionItem(column);
			selectAllItems.add(a);
		}

		for(ForeignKeyConstraint fkConstraint : this.databaseTable.getFkConstraints())
			this.fkConstraints.add(fkConstraint);
	}

	@Override
	public void scanTemporaryTables(List<Record> recordsList) throws SQLException
	{
		throw new NotCallableException(
				"AbstractExecAgent.scanTemporaryTables should not be called for this executor " + "agent");
	}

	@Override
	public void clearExecutor() throws SQLException
	{
		if(isDirty)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("DELETE FROM ");
			buffer.append(this.tempTableName);

			this.sqlInterface.executeUpdate(buffer.toString());
		}

		isDirty = false;
	}

	@Override
	public void setup(DatabaseMetaData metadata, int scratchpadId) throws ScratchpadException
	{
		try
		{
			this.tempTableName = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.databaseTable.getName() +
					"_" +
					scratchpadId;
			this.fromItemTemp = new Table(Environment.DATABASE_NAME, tempTableName);
			this.tempTableNameAlias = ScratchpadDefaults.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + this.tableId;
			INSERT_PREFIX = "INSERT INTO " + tempTableName + " (";

			String tableNameAlias = ScratchpadDefaults.SCRATCHPAD_TABLE_ALIAS_PREFIX + this.tableId;

			StringBuilder buffer2 = new StringBuilder();
			buffer2.append("DROP TABLE IF EXISTS ");
			StringBuilder buffer = new StringBuilder();

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append("CREATE LOCAL TEMPORARY TABLE ");    // for H2
			else
				buffer.append("CREATE TABLE IF NOT EXISTS ");        // for mysql

			if(LOG.isTraceEnabled())
				LOG.trace("creating temporary table {}", this.tempTableName);

			buffer.append(tempTableName);
			buffer2.append(tempTableName);
			buffer2.append(";");
			buffer.append("(");

			ArrayList<Boolean> tempIsStr = new ArrayList<>();        // for columns
			ArrayList<String> temp = new ArrayList<>();        // for columns
			ArrayList<String> tempAlias = new ArrayList<>();    // for columns with aliases
			ArrayList<String> tempTempAlias = new ArrayList<>();    // for temp columns with aliases
			ArrayList<String> uniqueIndices = new ArrayList<>(); // unique index
			ResultSet colSet = metadata.getColumns(null, null, this.databaseTable.getName(), "%");
			boolean first = true;

			while(colSet.next())
			{
				if(this.databaseTable.getField(colSet.getString(4)).isHiddenField())
					continue;

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
				if(colSet.getString(4).equalsIgnoreCase(DatabaseDefaults.DELETED_CLOCK_COLUMN))
				{
					buffer.append(" DEFAULT FALSE ");
				}

				temp.add(colSet.getString(4));
				tempAlias.add(tableNameAlias + "." + colSet.getString(4));
				tempTempAlias.add(this.tempTableNameAlias + "." + colSet.getString(4));
				tempIsStr.add(
						colSet.getInt(5) == Types.VARCHAR || colSet.getInt(5) == Types.LONGNVARCHAR || colSet.getInt(
								5) == Types.LONGVARCHAR || colSet.getInt(5) == Types.CHAR || colSet.getInt(
								5) == Types.DATE || colSet.getInt(5) == Types.TIMESTAMP || colSet.getInt(
								5) == Types.TIME);
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
			ResultSet uqIndices = metadata.getIndexInfo(null, null, this.databaseTable.getName(), true, true);
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

			ResultSet pkSet = metadata.getPrimaryKeys(null, null, this.databaseTable.getName());
			while(pkSet.next())
			{
				if(temp.size() == 0)
					buffer.append(", PRIMARY KEY (");
				else
					buffer.append(", ");
				buffer.append(pkSet.getString(4));
				temp.add(pkSet.getString(4));
				tempAlias.add(tableNameAlias + "." + pkSet.getString(4));
				tempTempAlias.add(this.tempTableNameAlias + "." + pkSet.getString(4));
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

			this.tableDefinition = new TableDefinition(this.databaseTable.getName(), tableNameAlias, this.tableId,
					colsIsStr, cols, aliasCols, tempAliasCols, pkPlain, pkAlias, pkTempAlias, uqIndicesPlain);

			if(ScratchpadDefaults.SQL_ENGINE == ScratchpadDefaults.RDBMS_H2)
				buffer.append(") NOT PERSISTENT;");    // FOR H2
			else
				buffer.append(") ENGINE=MEMORY;");    // FOR MYSQL

			this.sqlInterface.executeUpdate(buffer2.toString());
			this.sqlInterface.executeUpdate(buffer.toString());

		} catch(SQLException e)
		{
			throw new ScratchpadException("scratchpad failed to initialize: " + e.getMessage());
		}
		if(LOG.isTraceEnabled())
			LOG.trace("executor for table {} created", this.databaseTable.getName());
	}

}
