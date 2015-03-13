package database.invariants;

import database.util.DataField;
import runtime.Runtime;
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
