package client.execution.operation;


import common.database.Record;
import common.database.field.DataField;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;


/**
 * Created by dnlopes on 06/12/15.
 */
public class SQLDelete extends SQLWriteOperation
{

	private final Delete sqlStat;

	public SQLDelete(Delete sqlStat)
	{
		super(SQLOperationType.DELETE, sqlStat.getTable());

		this.sqlStat = sqlStat;
		this.record = new Record(this.dbTable);
	}

	public SQLDelete(Delete sqlStat, Record record)
	{
		super(SQLOperationType.DELETE, sqlStat.getTable());

		this.sqlStat = sqlStat;
		this.record = record;
	}

	@Override
	public void prepareOperation(boolean useWhere, String tempTableName)
	{

	}

	@Override
	public SQLOperation duplicate() throws JSQLParserException
	{
		return new SQLDelete(sqlStat, this.record.duplicate());
	}

	@Override
	public void prepareForNextInput()
	{

	}

	public boolean isPrimaryKeyMissingFromWhere()
	{
		String whereClause = this.sqlStat.getWhere().toString();

		for(DataField pkField : this.pk.getPrimaryKeyFields().values())
			if(!whereClause.contains(pkField.getFieldName()))
				return true;

		return false;
	}

	@Override
	public void addRecordEntry(String column, String value)
	{

	}

	public Delete getDelete()
	{
		return this.sqlStat;
	}
}
