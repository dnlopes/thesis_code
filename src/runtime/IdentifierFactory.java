package runtime;


import database.util.SemanticPolicy;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import nodes.NodeConfig;
import nodes.proxy.ProxyConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 */
public class IdentifierFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(IdentifierFactory.class);

	private static Map<String, IDGenerator> ID_GENERATORS_MAP;
	private static String REPLICA_PREFIX;
	private static int BOUNDED_REPLICATOR_ID;

	public static void setup(final ProxyConfig config)
	{
		ID_GENERATORS_MAP = new HashMap<>();
		BOUNDED_REPLICATOR_ID = config.getReplicatorConfig().getId();
		REPLICA_PREFIX = String.valueOf(BOUNDED_REPLICATOR_ID) + ":";

		if(LOG.isTraceEnabled())
			LOG.trace("bootstraping id generators for auto increment fields");

		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			List<DataField> fields = table.getFieldsList();

			for(DataField field : fields)
			{
				if(!field.isAutoIncrement())
					continue;

				if(field.getSemantic() == SemanticPolicy.NOSEMANTIC)
					createIdGenerator(field, config);
				else
					createTemporaryIdGenerator(field, config);
			}
		}
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
		if(LOG.isTraceEnabled())
			LOG.trace("new unique id generated for key {}: {}", key, id);
		return id;
	}

	public static String appendReplicaPrefix(String value)
	{
		StringBuilder buffer = new StringBuilder("'");
		buffer.append(REPLICA_PREFIX);
		buffer.append(StringUtils.substring(value, 1, value.length() - 1));
		buffer.append("'");

		return buffer.toString();
	}

	private static void createTemporaryIdGenerator(DataField field, NodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(ID_GENERATORS_MAP.containsKey(key))
		{
			LOG.warn("id generator already created. Silently ignored");
			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config, 1);

		ID_GENERATORS_MAP.put(key, newGenerator);

		if(LOG.isTraceEnabled())
			LOG.trace("temporary id generator for field {} created. Initial value {}", field.getFieldName(),
					newGenerator.getCurrentValue());
	}

	private static void createIdGenerator(DataField field, NodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(ID_GENERATORS_MAP.containsKey(key))
		{
			if(LOG.isWarnEnabled())
				LOG.warn("id generator already created. Silently ignored");

			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config, Configuration.getInstance().getReplicatorsCount());

		ID_GENERATORS_MAP.put(key, newGenerator);
		if(LOG.isTraceEnabled())
			LOG.trace("id generator for field {} created. Initial value {}", field.getFieldName(),
					newGenerator.getCurrentValue());
	}

}
