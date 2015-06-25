package runtime;


import database.util.field.DataField;
import database.util.table.DatabaseTable;
import nodes.NodeConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final String REPLICA_PREFIX = System.getProperty("proxyid") + ":";

	public static void createGenerators(NodeConfig config)
	{
		if(Configuration.INFO_ENABLED)
			LOG.info("bootstraping id generators for auto increment fields");
		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			List<DataField> fields = table.getFieldsList();

			for(DataField field : fields)
			{
				if(field.isNumberField())
					createIdGenerator(field, config);
			}
		}
	}

	private static void createIdGenerator(DataField field, NodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(ID_GENERATORS_MAP.containsKey(key))
		{
			LOG.warn("id generator already created. Silently ignored");
			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config);

		ID_GENERATORS_MAP.put(key, newGenerator);
		if(Configuration.TRACE_ENABLED)
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
			RuntimeUtils.throwRunTimeException("id generator not found for key " + key, ExitCode.ID_GENERATOR_ERROR);

		int id = ID_GENERATORS_MAP.get(key).getNextId();
		if(Configuration.TRACE_ENABLED)
			LOG.trace("new unique id generated for key {}: {}", key, id);
		return id;
	}

	public static String appendReplicaPrefix(String value)
	{
		StringBuilder buffer = new StringBuilder("'");
		buffer.append(REPLICA_PREFIX);
		buffer.append(StringUtils.substring(value, 1, value.length()-1));
		buffer.append("'");

		return buffer.toString();
	}
}
