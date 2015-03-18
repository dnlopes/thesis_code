package database.invariants;

import database.util.DataField;
import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 10/03/15.
 */
public class GreaterThanInvariant extends Invariant
{

	private final String minValue;
	private boolean equal;

	public GreaterThanInvariant(DataField field, String threshold)
	{
		super();
		this.fields.add(field);
		this.minValue = threshold;
		this.equal = false;
	}

	public String getMinValue()
	{
		return this.minValue;
	}

	@Override
	public void addField(DataField field)
	{
		RuntimeHelper.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void addPair(DataField field, String remoteField)
	{
		RuntimeHelper.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public void setRemoteTable(String table)
	{
		RuntimeHelper.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	@Override
	public String getRemoteTable()
	{
		RuntimeHelper.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
		return null;
	}

	public boolean isViolated(String value)
	{
		if(this.equal)
			return value.compareTo(this.minValue) < 0;

		return value.compareTo(this.minValue) <= 0;
	}

	public void setEqual()
	{
		this.equal = true;
	}
}
