package runtime.txn;


import database.util.DatabaseTable;
import database.util.FieldValue;
import database.util.PrimaryKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 21/03/15.
 */
public class RowWriteSet
{

	protected static final Logger LOG = LoggerFactory.getLogger(RowWriteSet.class);
	protected PrimaryKeyValue pkValue;
	protected Map<String, FieldValue> newContent;
	protected Map<String, FieldValue> oldContent;
	protected DatabaseTable table;

	public RowWriteSet(PrimaryKeyValue pkValue, DatabaseTable table)
	{
		this.pkValue = pkValue;
		this.table = table;
		this.newContent = new LinkedHashMap<>();
		this.oldContent = new LinkedHashMap<>();
	}

	public void addNewContent(String fieldName, FieldValue lwwValue)
	{
		this.newContent.put(fieldName, lwwValue);
	}

	public void addOldContent(String fieldName, FieldValue oldValue)
	{
		this.oldContent.put(fieldName, oldValue);
	}

	public PrimaryKeyValue getTuplePkValue()
	{
		return this.pkValue;
	}

	public Map<String, FieldValue> getModifiedValuesMap()
	{
		return this.newContent;
	}

	public Map<String, FieldValue> getOldValuesMap()
	{
		return this.oldContent;
	}

	public void generateUpdateStatement(List<String> statements)
	{
		if(newContent.size() == 0)
			return;

		LOG.debug("{} fields modified for tuple {}", this.newContent.size(), this.pkValue.getValue());
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(this.table.getName());
		buffer.append(" set ");

		Iterator<String> modifiedFieldsIterator = this.newContent.keySet().iterator();

		while(modifiedFieldsIterator.hasNext())
		{
			String fieldName = modifiedFieldsIterator.next();
			FieldValue newValue = this.newContent.get(fieldName);
			buffer.append(fieldName);
			buffer.append("=");
			buffer.append(newValue);

			if(modifiedFieldsIterator.hasNext())
				buffer.append(",");
		}

		buffer.append(" WHERE (");
		buffer.append(this.table.getPrimaryKey().getQueryClause());
		buffer.append(") = (");
		buffer.append(this.pkValue.getValue());
		buffer.append(")");

		/*buffer.append(") AND (");
		buffer.append(CLOCK_PREBUILT_CLAUSE);
		buffer.append(")");        */

		String statement = buffer.toString();
		LOG.trace("update statement generated: {}", statement);

		statements.add(statement);
	}

	public void generateInsertStatement(List<String> statements)
	{
		StringBuilder buffer = new StringBuilder();
		StringBuffer colsBuffer = new StringBuffer();
		StringBuffer valsBuffer = new StringBuffer();

		//if(this.table.getTableType() == CrdtTableType.ARSETTABLE)
			//this.newContent.put(table.getDeletedField().getFieldName(), "FALSE");

		/*
		if(this.table.getTableType() != CrdtTableType.NONCRDTTABLE)
		{
			this.newContent.put(table.getClockGenerationField().getFieldName(),
					DBDefaults.CLOCK_GENERATION_PLACEHOLDER);
			this.newContent.put(table.getContentClockField().getFieldName(), DBDefaults
			.CLOCK_VALUE_PLACEHOLDER);
		}   */

		buffer.append("insert into ");
		buffer.append(this.table.getName());

		Iterator<String> modifiedFieldsIterator = this.newContent.keySet().iterator();

		while(modifiedFieldsIterator.hasNext())
		{
			String fieldName = modifiedFieldsIterator.next();
			FieldValue newValue = this.newContent.get(fieldName);
			colsBuffer.append(fieldName);
			valsBuffer.append(newValue);
			if(modifiedFieldsIterator.hasNext())
			{
				colsBuffer.append(",");
				valsBuffer.append(",");
			}
		}

		buffer.append(" (");
		buffer.append(colsBuffer.toString());
		buffer.append(") VALUES (");
		buffer.append(valsBuffer.toString());
		buffer.append(")");

		String statement = buffer.toString();
		LOG.debug("insert statement generated: {}", statement);

		statements.add(statement);
	}
}
