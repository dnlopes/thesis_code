package runtime.factory;


import database.util.DataField;
import database.util.DatabaseTable;
import network.proxy.ProxyConfig;
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

	public static void setupIdentifiersGenerators(ProxyConfig config)
	{
		LOG.info("bootstrap id generators for auto increment fields");
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

	public static void createIdGenerator(DataField field, ProxyConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(ID_GENERATORS_MAP.containsKey(key))
		{
			LOG.warn("id generator already created. Silently ignoring");
			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config);

		ID_GENERATORS_MAP.put(key, newGenerator);
		LOG.info("id generator for field {} created. Initial value {}", field.getFieldName(),
				newGenerator.getCurrentValue());
	}

	public static int getNextId(DataField field)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(!ID_GENERATORS_MAP.containsKey(key))
			RuntimeHelper.throwRunTimeException("id generator not found", ExitCode.ID_GENERATOR_ERROR);

		return ID_GENERATORS_MAP.get(key).getNextId();
	}

	public static IDGenerator getIdGenerator(DataField field)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(!ID_GENERATORS_MAP.containsKey(key))
			RuntimeHelper.throwRunTimeException("id generator not found", ExitCode.ID_GENERATOR_ERROR);

		return ID_GENERATORS_MAP.get(key);
	}

}
