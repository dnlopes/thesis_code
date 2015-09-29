package database.execution.util;


/**
 * Created by dnlopes on 17/09/15.
 */
public class CRDTUpdateOperation
{

	private final String operation;
	private final String clock;
	private final String tupleKey;

	public CRDTUpdateOperation(String sqlStatement, String clock, String key)
	{
		this.operation = sqlStatement;
		this.clock = clock;
		this.tupleKey = key;
	}

	public String getOperation()
	{
		return this.operation;
	}

	public String getClock()
	{
		return this.clock;
	}

	public String getTupleKey()
	{
		return this.tupleKey;
	}
	
}
