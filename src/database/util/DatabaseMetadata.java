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

	private Map<String, DatabaseTable> tables;

	public DatabaseMetadata()
	{
		this.tables = new HashMap<>();
	}

	public DatabaseTable getTable(String tableName)
	{
		return tables.get(tableName);
	}

	public void addTable(DatabaseTable table)
	{
		if(this.tables.containsKey(table.getTableName()))
		{
			LOG.error("table {} already exists", table.getTableName());
			RuntimeHelper.throwRunTimeException("duplicated table", ExitCode.UNEXPECTED_TABLE);
		}

		this.tables.put(table.getTableName(), table);
		LOG.trace("table {} added", table.getTableName());
	}

}
