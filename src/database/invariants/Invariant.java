package database.invariants;

import database.util.DataField;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by dnlopes on 06/03/15.
 */
public abstract class Invariant
{

	protected List<DataField> fields;

	protected Invariant()
	{
		this.fields = new LinkedList<>();
	}

	public List<DataField> getFields()
	{
		return this.fields;
	}

	public abstract void addField(DataField field);
	public abstract void addPair(DataField field, String remoteField);
	public abstract void setRemoteTable(String table);
	public abstract String getRemoteTable();

}
