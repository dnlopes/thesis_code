package database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.*;
import runtime.Runtime;
import util.ExitCode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 12/03/15.
 */
public class Database
{

	static final Logger LOG = LoggerFactory.getLogger(Database.class);

	private static Database ourInstance = new Database();
	private Map<String, DatabaseTable> tables;

	public static Database getInstance()
	{
		return ourInstance;
	}

	private Database()
	{
		this.tables = new HashMap<>();
	}

	public DatabaseTable getTable(String tableName)
	{
		return tables.get(tableName);
	}

	public Collection<DatabaseTable> getTables()
	{
		return this.tables.values();
	}

	public void addTable(DatabaseTable table)
	{
		if(this.tables.containsKey(table.getTableName()))
		{
			LOG.error("table {} already exists", table.getTableName());
			Runtime.throwRunTimeException("duplicated table", ExitCode.UNEXPECTED_TABLE);
		}

		this.tables.put(table.getTableName(), table);
		LOG.trace("table {} added", table.getTableName());
	}

}
