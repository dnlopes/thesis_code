package runtime.txn;


import database.util.CrdtTableType;
import database.util.DatabaseTable;
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
public class TupleWriteSet
{

	private static final Logger LOG = LoggerFactory.getLogger(TupleWriteSet.class);
	private PrimaryKeyValue tupleId;
	private Map<String, String> lwwFieldsValues;
	private Map<String, String> oldFieldsValues;
	private DatabaseTable table;

	public TupleWriteSet(PrimaryKeyValue tuplePkValue, DatabaseTable table)
	{
		this.tupleId = tuplePkValue;
		this.table = table;
		this.lwwFieldsValues = new LinkedHashMap<>();
		this.oldFieldsValues = new LinkedHashMap<>();
	}

	public void addLwwEntry(String fieldName, String lwwValue)
	{
		this.lwwFieldsValues.put(fieldName, lwwValue);
	}

	public void addOldEntry(String fieldName, String oldValue)
	{
		this.oldFieldsValues.put(fieldName, oldValue);
	}

	public PrimaryKeyValue getTuplePkValue()
	{
		return this.tupleId;
	}

	public Map<String, String> getModifiedValuesMap()
	{
		return this.lwwFieldsValues;
	}

	public Map<String, String> getOldValuesMap()
	{
		return this.oldFieldsValues;
	}

	public void generateUpdateStatement(List<String> statements)
	{
		if(lwwFieldsValues.size() == 0)
			return;

		LOG.debug("{} fields modified for tuple {}", this.lwwFieldsValues.size(), this.tupleId.getValue());
		StringBuilder buffer = new StringBuilder();
		buffer.append("UPDATE ");
		buffer.append(this.table.getName());
		buffer.append(" set ");

		Iterator<String> modifiedFieldsIterator = this.lwwFieldsValues.keySet().iterator();

		while(modifiedFieldsIterator.hasNext())
		{
			String fieldName = modifiedFieldsIterator.next();
			String newValue = this.lwwFieldsValues.get(fieldName);
			buffer.append(fieldName);
			buffer.append("=");
			buffer.append(newValue);

			if(modifiedFieldsIterator.hasNext())
				buffer.append(",");
		}

		buffer.append(" WHERE (");
		buffer.append(this.table.getPrimaryKey().getQueryClause());
		buffer.append(") = (");
		buffer.append(this.tupleId.getValue());
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

		if(this.table.getTableType() == CrdtTableType.ARSETTABLE)
			this.lwwFieldsValues.put(table.getDeletedField().getFieldName(), "FALSE");

		/*
		if(this.table.getTableType() != CrdtTableType.NONCRDTTABLE)
		{
			this.lwwFieldsValues.put(table.getClockGenerationField().getFieldName(),
					DBDefaults.CLOCK_GENERATION_PLACEHOLDER);
			this.lwwFieldsValues.put(table.getContentClockField().getFieldName(), DBDefaults
			.CLOCK_VALUE_PLACEHOLDER);
		}   */

		buffer.append("insert into ");
		buffer.append(this.table.getName());

		Iterator<String> modifiedFieldsIterator = this.lwwFieldsValues.keySet().iterator();

		while(modifiedFieldsIterator.hasNext())
		{
			String fieldName = modifiedFieldsIterator.next();
			String newValue = this.lwwFieldsValues.get(fieldName);
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
