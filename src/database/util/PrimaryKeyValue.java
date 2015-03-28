package database.util;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;
import java.util.List;


/**
 * Created by dnlopes on 28/03/15.
 */
public final class PrimaryKeyValue
{

	private List<String> fieldsValues;
	private final String valuesClause;

	public PrimaryKeyValue(List<String> values)
	{
		this.fieldsValues = values;
		this.valuesClause = this.generateValue();
	}

	public String getValue()
	{
		return this.valuesClause;
	}

	private String generateValue()
	{
		StringBuilder buffer = new StringBuilder("(");

		Iterator<String> it = this.fieldsValues.iterator();
		while(it.hasNext())
		{
			buffer.append(it.next());
			if(it.hasNext())
				buffer.append(",");
		}

		buffer.append(")");
		return buffer.toString();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
				append(valuesClause).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof PrimaryKeyValue))
			return false;
		if(obj == this)
			return true;

		PrimaryKeyValue otherObject = (PrimaryKeyValue) obj;
		return new EqualsBuilder().
				append(this.valuesClause, otherObject.getValue()).
				isEquals();
	}
	
}
