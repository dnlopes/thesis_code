package database.execution.util;


/**
 * Created by dnlopes on 17/09/15.
 */
public class DBWriteSetEntry implements DBEntry
{

	public String key;

	@Override
	public String getKey()
	{
		return this.key;
	}
}
