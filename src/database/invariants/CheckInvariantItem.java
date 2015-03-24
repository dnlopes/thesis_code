package database.invariants;


/**
 * Created by dnlopes on 18/03/15.
 */
public abstract class CheckInvariantItem
{
	private InvariantType type;
	protected int rowId;
	private int itemId;
	private String fieldName;
	private String tableName;


	public CheckInvariantItem(InvariantType type, int rowId, int id, String fieldName, String tableName)
	{
		this.type = type;
		this.rowId = rowId;
		this.itemId = id;
		this.fieldName = fieldName;
		this.tableName = tableName;
	}

	public int getRowId()
	{
		return rowId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public InvariantType getType()
	{
		return this.type;

	}
}
