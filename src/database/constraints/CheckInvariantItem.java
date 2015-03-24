package database.constraints;


/**
 * Created by dnlopes on 18/03/15.
 */
public abstract class CheckInvariantItem
{
	private ConstraintType type;
	protected int rowId;
	private int itemId;
	private String fieldName;
	private String tableName;


	public CheckInvariantItem(ConstraintType type, int rowId, int id, String fieldName, String tableName)
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

	public ConstraintType getType()
	{
		return this.type;

	}
}
