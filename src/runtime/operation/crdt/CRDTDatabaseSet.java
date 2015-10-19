package runtime.operation.crdt;


import database.util.DatabaseMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.operation.OperationsStatements;
import util.Configuration;
import util.defaults.DatabaseDefaults;
import util.thrift.CRDTOperation;

import java.util.Map;


/**
 * Created by dnlopes on 29/09/15.
 */
public class CRDTDatabaseSet
{
	private static final Logger LOG = LoggerFactory.getLogger(CRDTDatabaseSet.class);
	private static final DatabaseMetadata METADATA = Configuration.getInstance().getDatabaseMetadata();


	public static String[] insertRow(CRDTOperation op, String clock)
	{
		op.putToNewFieldValues(DatabaseDefaults.DELETED_COLUMN, DatabaseDefaults.NOT_DELETED_VALUE);
		op.putToNewFieldValues(DatabaseDefaults.CONTENT_CLOCK_COLUMN, clock);
		op.putToNewFieldValues(DatabaseDefaults.DELETED_CLOCK_COLUMN, clock);

		StringBuilder buffer = new StringBuilder();
		StringBuilder valuesBuffer = new StringBuilder();

		buffer.append(OperationsStatements.INSERT_INTO);
		buffer.append(op.getTableName());
		buffer.append(" (");

		for(Map.Entry<String, String> entry : op.getNewFieldValues().entrySet())
		{
			buffer.append(entry.getKey());
			valuesBuffer.append(entry.getValue());
		}

		buffer.setLength(buffer.length()-1);
		valuesBuffer.setLength(valuesBuffer.length()-1);

		buffer.append(OperationsStatements.PARENT_VALUES_PARENT);
		buffer.append(valuesBuffer.toString());
		buffer.append(")");

		String[] ops = new String[1];
		ops[0] = buffer.toString();

		return ops;
	}

	public static String[] insertChildRow(CRDTOperation op, String clock)
	{
		if(!op.isSetParentsMap())
			return insertRow(op, clock);

		return null;
	}

	public static String[] updateRow(CRDTOperation op, String clock)
	{
		return null;
	}

	public static String[] updateChildRow(CRDTOperation op, String clock)
	{
		return null;
	}

	public static String[] deleteRow(CRDTOperation op, String clock)
	{
		return null;
	}

	public static String[] deleteParentRow(CRDTOperation op, String clock)
	{
		return null;
	}

}
