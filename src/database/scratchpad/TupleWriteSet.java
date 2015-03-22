package database.scratchpad;


import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public class TupleWriteSet
{

	private String tupleId;

	// maps fields name and LWW value
	private Map<String, String> tupleValues;

	public TupleWriteSet(String tupleId)
	{
		this.tupleId = tupleId;
		this.tupleValues = new HashMap<>();
	}

	public void addEntry(String fieldName, String value)
	{
		this.tupleValues.put(fieldName,value);
	}

	public String getTupleId()
	{
		return this.tupleId;
	}

	public Map<String, String> getWriteSet()
	{
		return this.tupleValues;
	}
	
}
