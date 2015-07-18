package database.util;


import database.util.value.FieldValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by dnlopes on 28/03/15.
 */
public final class PrimaryKeyValue
{

	private String tableName;
	private Map<String, FieldValue> values;
	private String uniqueValue;
	private String primaryKeyWhereClause;
	private String pkValue;
	private boolean isUniqueGenerated;
	private boolean isPkGenerated;
	private boolean isValueGenerated;

	public PrimaryKeyValue(String tableName)
	{
		this.tableName = tableName;
		this.values = new HashMap<>();
		this.isUniqueGenerated = false;
		this.isPkGenerated = false;
		this.isValueGenerated = false;
	}

	public String getUniqueValue()
	{
		if(!isUniqueGenerated)
			this.generateUniqueIdentifier();

		return this.uniqueValue;
	}

	public void addFieldValue(FieldValue fieldValue)
	{
		this.values.put(fieldValue.getDataField().getFieldName(), fieldValue);
		this.isUniqueGenerated = false;
	}

	public FieldValue getFieldValue(String fieldName)
	{
		return this.values.get(fieldName);
	}

	public String getTableName()
	{
		return this.tableName;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
				append(this.uniqueValue).
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
				append(this.uniqueValue, otherObject.getUniqueValue()).
				isEquals();
	}

	private void generateUniqueIdentifier()
	{
		StringBuilder buffer = new StringBuilder(this.tableName);
		buffer.append(":");

		Iterator<FieldValue> it = values.values().iterator();
		while(it.hasNext())
		{
			FieldValue fValue = it.next();
			buffer.append(fValue.getDataField().getFieldName());
			buffer.append("=");
			buffer.append(fValue.getFormattedValue());
			if(it.hasNext())
				buffer.append(",");
		}

		this.isUniqueGenerated = true;
		this.uniqueValue = buffer.toString();
	}

	public String getPrimaryKeyWhereClause()
	{
		if(!isPkGenerated)
			this.generatePkWhereClause();

		return this.primaryKeyWhereClause;
	}

	private void generatePkWhereClause()
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<FieldValue> it = values.values().iterator();
		while(it.hasNext())
		{
			FieldValue fValue = it.next();
			buffer.append(fValue.getDataField().getFieldName());
			buffer.append("=");
			buffer.append(fValue.getFormattedValue());
			if(it.hasNext())
				buffer.append(" AND ");
		}

		this.isPkGenerated = true;
		this.primaryKeyWhereClause = buffer.toString();
	}

	private void generateValue()
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<FieldValue> it = values.values().iterator();
		while(it.hasNext())
		{
			FieldValue fValue = it.next();
			buffer.append(fValue.getFormattedValue());
			if(it.hasNext())
				buffer.append(",");
		}

		this.isValueGenerated = true;
		this.pkValue = buffer.toString();
	}


	public String getValue()
	{
		if(!isValueGenerated)
			this.generateValue();

		return this.pkValue;
	}
}
