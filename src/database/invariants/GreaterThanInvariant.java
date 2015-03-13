package database.invariants;

import database.util.DataField;
import runtime.Runtime;
import util.ExitCode;


/**
 * Created by dnlopes on 10/03/15.
 */
public class GreaterThanInvariant extends Invariant
{

	private String minValue;

	public GreaterThanInvariant(DataField field, String threshold)
	{
		super();
		this.fields.add(field);
		this.minValue = threshold;
	}

	public String getMinValue()
	{
		return this.minValue;
	}

	@Override
	public void addField(DataField field)
	{
		runtime.Runtime.throwRunTimeException("should not be calling this method", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void addPair(DataField field, String remoteField)
	{
		Runtime.throwRunTimeException("should not be calling this method", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void setRemoteTable(String table)
	{
		Runtime.throwRunTimeException("should not be calling this method", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public String getRemoteTable()
	{
		Runtime.throwRunTimeException("should not be calling this method", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
}
