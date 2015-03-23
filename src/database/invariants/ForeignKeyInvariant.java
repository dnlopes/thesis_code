package database.invariants;

import database.util.DataField;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by dnlopes on 06/03/15.
 */
public class ForeignKeyInvariant extends Invariant
{

	private List<String> remoteFields;
	private String remoteTable;

	public ForeignKeyInvariant()
	{
		super(InvariantType.FOREIGN_KEY);
		this.remoteFields = new LinkedList<>();
	}

	@Override
	public void addField(DataField field)
	{
		RuntimeHelper.throwRunTimeException("this method should not be called", ExitCode.MISSING_IMPLEMENTATION);
	}

	public void addPair(DataField origin, String remote)
	{
		this.fields.add(fields.size(), origin);
		this.remoteFields.add(this.remoteFields.size(), remote);
	}

	public void setRemoteTable(String table)
	{
		if(this.remoteTable == null)
			this.remoteTable = table;
	}

	public String getRemoteTable()
	{
		return this.remoteTable;
	}
}
