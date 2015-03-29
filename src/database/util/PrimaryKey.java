package database.util;


import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 28/03/15.
 */
public class PrimaryKey
{

	private List<DataField> fields;
	private final String queryClause;
	private final int pkSize;

	public PrimaryKey(List<DataField> fields)
	{
		this.fields = fields;

		this.pkSize = fields.size();
		this.queryClause = this.generateQueryClause();

	}

	public int getSize()
	{
		return this.pkSize;
	}

	public String getQueryClause()
	{
		return this.queryClause;
	}

	private String generateQueryClause()
	{
		StringBuilder buffer = new StringBuilder("(");

		Iterator<DataField> it = this.fields.iterator();
		while(it.hasNext())
		{
			buffer.append(it.next().getFieldName());
			if(it.hasNext())
				buffer.append(",");
		}

		buffer.append(")");
		return buffer.toString();
	}
	
}
