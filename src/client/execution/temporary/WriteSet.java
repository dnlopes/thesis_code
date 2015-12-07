package client.execution.temporary;


import common.database.Record;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by dnlopes on 06/12/15.
 */
public class WriteSet
{
	private Set<Record> inserts;
	private Set<Record> updates;
	private Set<Record> deletes;

	public WriteSet()
	{
		this.inserts = new HashSet<>();
		this.updates = new HashSet<>();
		this.deletes= new HashSet<>();
	}

	public void addToInserts(Record record)
	{
		this.inserts.add(record);
	}

	public void addToUpdates(Record record)
	{
		this.updates.add(record);
	}

	public void addToDeletes(Record record)
	{
		this.deletes.add(record);
	}

	public void reset()
	{
		this.inserts.clear();
		this.updates.clear();
		this.deletes.clear();;
	}
	
}
