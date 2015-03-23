package database.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 12/03/15.
 */
public class DatabaseMetadata
{

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseMetadata.class);

	private static Map<String, DatabaseTable> ALL_TABLES;

	public DatabaseMetadata()
	{
		ALL_TABLES = new HashMap<>();
	}

	public DatabaseTable getTable(String tableName)
	{
		return ALL_TABLES.get(tableName);
	}

	public void addTable(DatabaseTable table)
	{
		if(ALL_TABLES.containsKey(table.getName()))
		{
			LOG.error("table {} already exists", table.getName());
			RuntimeHelper.throwRunTimeException("duplicated table", ExitCode.UNEXPECTED_TABLE);
		}

		ALL_TABLES.put(table.getName(), table);
		LOG.trace("table {} added", table.getName());
	}

	public static DataField getField(String tableName, String fieldName)
	{
		return ALL_TABLES.get(tableName).getField(fieldName);
	}

}
