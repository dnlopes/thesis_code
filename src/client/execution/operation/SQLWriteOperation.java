package client.execution.operation;


import common.database.Record;
import common.database.table.DatabaseTable;
import common.database.util.PrimaryKey;
import common.database.util.PrimaryKeyValue;
import net.sf.jsqlparser.schema.Table;


/**
 * Created by dnlopes on 06/12/15.
 */
public abstract class SQLWriteOperation extends SQLOperation
{

	protected final DatabaseTable dbTable;
	protected final Table table;
	protected final PrimaryKey pk;
	protected Record record;
	protected boolean isPrimaryKeySet;

	public SQLWriteOperation(SQLOperationType type, Table table)
	{
		super(type);
		this.table = table;
		this.dbTable = DB_METADATA.getTable(table.getName());
		this.pk = dbTable.getPrimaryKey();
		this.isPrimaryKeySet = false;
	}

	public abstract void prepareForNextInput();
	public abstract void addRecordEntry(String column, String value);

	public DatabaseTable getDbTable()
	{
		return dbTable;
	}

	public Table getTable()
	{
		return this.table;
	}

	public PrimaryKey getPk()
	{
		return pk;
	}

	public Record getRecord()
	{
		return record;
	}

	public boolean isPrimaryKeySet()
	{
		return isPrimaryKeySet;
	}

	public void setPrimaryKey(PrimaryKeyValue pkValue)
	{
		record.setPkValue(pkValue);
		isPrimaryKeySet = true;
	}
}
