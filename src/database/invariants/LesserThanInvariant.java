package database.invariants;

import database.util.DataField;
import runtime.Runtime;
import util.ExitCode;


/**
 * Created by dnlopes on 06/03/15.
 */
public class LesserThanInvariant extends Invariant
{

	private String maxValue;

	public LesserThanInvariant(DataField field, String maxValue)
	{
		super();
		this.fields.add(field);
		this.maxValue = maxValue;
	}

	public String getMaxValue()
	{
		return this.maxValue;
	}

	@Override
	public void addField(DataField field)
	{
		Runtime.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void addPair(DataField field, String remoteField)
	{
		Runtime.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void setRemoteTable(String table)
	{
		Runtime.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public String getRemoteTable()
	{
		Runtime.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}
}
