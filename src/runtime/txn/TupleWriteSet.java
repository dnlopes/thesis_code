package runtime.txn;


import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TupleWriteSet
{

	private Integer tupleId;
	private Map<String, String> lwwFieldsValues;
	private Map<String, String> oldFieldsValues;


	public TupleWriteSet(Integer tupleId)
	{
		this.tupleId = tupleId;
		this.lwwFieldsValues = new LinkedHashMap<>();
		this.oldFieldsValues = new LinkedHashMap<>();
	}

	public void addLwwEntry(String fieldName, String lwwValue)
	{
		this.lwwFieldsValues.put(fieldName, lwwValue);
	}

	public void addOldEntry(String fieldName, String oldValue)
	{
		this.oldFieldsValues.put(fieldName, oldValue);
	}

	public Integer getTupleId()
	{
		return this.tupleId;
	}

	public Map<String, String> getModifiedValuesMap()
	{
		return this.lwwFieldsValues;
	}

	public Map<String, String> getOldValuesMap()
	{
		return this.oldFieldsValues;
	}

	public String getNewValue(String fieldName)
	{
		return this.lwwFieldsValues.get(fieldName);
	}

	public String getOldValue(String fieldName)
	{
		return this.oldFieldsValues.get(fieldName);
	}
}