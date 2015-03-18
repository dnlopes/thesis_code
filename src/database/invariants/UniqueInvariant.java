package database.invariants;

import database.util.DataField;
import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 06/03/15.
 */
public class UniqueInvariant extends Invariant
{

	public UniqueInvariant()
	{
		super();
	}

	public void addField(DataField field)
	{
		this.fields.add(field);
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
}
