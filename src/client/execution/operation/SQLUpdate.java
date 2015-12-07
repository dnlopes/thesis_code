package client.execution.operation;


import common.database.Record;
import common.database.field.DataField;
import common.database.util.PrimaryKeyValue;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.update.Update;


/**
 * Created by dnlopes on 06/12/15.
 */
public class SQLUpdate extends SQLWriteOperation
{

	private final Update sqlStat;
	private Record cachedRecord;
	private StringBuilder sqlBuffer;

	public SQLUpdate(Update sqlStat) throws JSQLParserException
	{
		super(SQLOperationType.UPDATE, sqlStat.getTables().get(0));

		if(sqlStat.getTables().size() != 1)
			throw new JSQLParserException("multi-table updates not supported");

		this.sqlStat = sqlStat;
		this.sqlBuffer = new StringBuilder("");
		this.record = new Record(this.dbTable);
		this.cachedRecord = new Record(this.dbTable);
	}

	public SQLUpdate(Update sqlStat, Record record) throws JSQLParserException
	{
		super(SQLOperationType.UPDATE, sqlStat.getTables().get(0));

		if(sqlStat.getTables().size() != 1)
			throw new JSQLParserException("multi-table updates not supported");

		this.sqlStat = sqlStat;
		this.record = record;
		this.sqlBuffer = new StringBuilder("");
		this.cachedRecord = new Record(this.dbTable);
	}

	public SQLUpdate(Update sqlStat, Record record, StringBuilder sqlBuffer) throws JSQLParserException
	{
		super(SQLOperationType.UPDATE, sqlStat.getTables().get(0));

		if(sqlStat.getTables().size() != 1)
			throw new JSQLParserException("multi-table updates not supported");

		this.sqlStat = sqlStat;
		this.record = record.duplicate();
		this.sqlBuffer = new StringBuilder(sqlBuffer);
	}

	@Override
	public void prepareOperation(boolean useWhere, String tempTableName)
	{
		StringBuilder sqlOpBuffer = new StringBuilder("UPDATE ").append(tempTableName).append(" SET ");
		sqlOpBuffer.append(this.sqlBuffer);

		sqlOpBuffer.append(" WHERE ");
		if(useWhere)
			sqlOpBuffer.append(sqlStat.getWhere().toString());
		else
			sqlOpBuffer.append(record.getPkValue().getPrimaryKeyWhereClause());

		this.sqlString = sqlOpBuffer.toString();
	}

	@Override
	public void prepareForNextInput()
	{
		this.sqlBuffer.append(",");
	}

	@Override
	public void addRecordEntry(String column, String value)
	{
		this.record.addData(column, value);
		this.sqlBuffer.append(" ").append(column).append("=").append(value);
	}

	@Override
	public void setPrimaryKey(PrimaryKeyValue pkValue)
	{
		super.setPrimaryKey(pkValue);
		cachedRecord.setPkValue(pkValue);
	}

	public Update getUpdate()
	{
		return sqlStat;
	}

	public boolean isPrimaryKeyMissingFromWhere()
	{
		String whereClause = this.sqlStat.getWhere().toString();

		for(DataField pkField : this.pk.getPrimaryKeyFields().values())
			if(!whereClause.contains(pkField.getFieldName()))
				return true;

		return false;
	}

	public SQLOperation duplicate() throws JSQLParserException
	{
		return new SQLUpdate(sqlStat, record, sqlBuffer);
	}

	public Record getCachedRecord()
	{
		return cachedRecord;
	}

	public void setCachedRecord(Record cachedRecord)
	{
		this.cachedRecord = cachedRecord;
	}

}
