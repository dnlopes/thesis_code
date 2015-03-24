package database.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 12/03/15.
 */
public class DatabaseMetadata
{

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseMetadata.class);

	private Map<String, DatabaseTable> tablesMap;

	public DatabaseMetadata()
	{
		tablesMap = new HashMap<>();
	}

	public DatabaseTable getTable(String tableName)
	{
		return tablesMap.get(tableName);
	}

	public void addTable(DatabaseTable table)
	{
		if(tablesMap.containsKey(table.getName()))
		{
			LOG.error("table {} already exists", table.getName());
			RuntimeHelper.throwRunTimeException("duplicated table", ExitCode.UNEXPECTED_TABLE);
		}

		tablesMap.put(table.getName(), table);
		LOG.trace("table {} added", table.getName());
	}

	public Collection<DatabaseTable> getAllTables()
	{
		return this.tablesMap.values();
	}

}
