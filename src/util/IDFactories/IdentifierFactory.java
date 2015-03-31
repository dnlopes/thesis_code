package util.IDFactories;


import database.util.DataField;
import database.util.DatabaseTable;
import network.AbstractNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.IDGenerator;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 */
public class IdentifierFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(IdentifierFactory.class);
	private static final Map<String, IDGenerator> ID_GENERATORS_MAP = new HashMap<>();

	public static void createGenerators(AbstractNodeConfig config)
	{
		LOG.info("bootstraping id generators for auto increment fields");
		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			List<DataField> fields = table.getFieldsList();

			for(DataField field : fields)
			{
				if(field.isAutoIncrement())
					createIdGenerator(field, config);
			}
		}
	}

	public static void createIdGenerator(DataField field, AbstractNodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(ID_GENERATORS_MAP.containsKey(key))
		{
			LOG.warn("id generator already created. Silently ignored");
			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config);

		ID_GENERATORS_MAP.put(key, newGenerator);
		LOG.trace("id generator for field {} created. Initial value {}", field.getFieldName(),
				newGenerator.getCurrentValue());
	}

	public static int getNextId(DataField field)
	{
		return getNextId(field.getTableName(), field.getFieldName());
	}

	public static int getNextId(String tableName, String fieldName)
	{
		String key = tableName + "_" + fieldName;

		if(!ID_GENERATORS_MAP.containsKey(key))
			RuntimeHelper.throwRunTimeException("id generator not found for key " + key, ExitCode.ID_GENERATOR_ERROR);

		int id =  ID_GENERATORS_MAP.get(key).getNextId();
		LOG.trace("new unique id generated for key {}: {}", key, id);
		return id;
	}

	public static IDGenerator getIdGenerator(String tableName, String fieldName)
	{
		String key = tableName + "_" + fieldName;

		if(!ID_GENERATORS_MAP.containsKey(key))
			RuntimeHelper.throwRunTimeException("id generator not found", ExitCode.ID_GENERATOR_ERROR);

		return ID_GENERATORS_MAP.get(key);
	}

}
