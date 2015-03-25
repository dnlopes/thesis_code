package runtime.factory;


import database.constraints.Constraint;
import database.constraints.ConstraintType;
import database.util.DataField;
import database.util.DatabaseTable;
import network.NodeMetadata;
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
	private static Map<String, IDGenerator> ID_GENERATORS;

	public IdentifierFactory(NodeMetadata nodeMetadata)
	{
		LOG.info("setup id generators for auto increment fields");
		ID_GENERATORS = new HashMap<>();
		this.setup(nodeMetadata);
	}

	public static void createIdGenerator(DataField field, NodeMetadata nodeMetadata)
	{
		IDGenerator newGenerator = new IDGenerator(field, nodeMetadata);
		String key = field.getTableName() + "_" + field.getFieldName();

		ID_GENERATORS.put(key, newGenerator);
		LOG.info("id generator for field {} created. Initial value {}", field.getFieldName(),
				newGenerator.getCurrentValue());
	}

	public static int getNextId(DataField field)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(!ID_GENERATORS.containsKey(key))
			RuntimeHelper.throwRunTimeException("id generator not found", ExitCode.ID_GENERATOR_ERROR);

		return ID_GENERATORS.get(key).getNextId();
	}

	private void setup(NodeMetadata nodeMetadata)
	{
		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			List<DataField> fields = table.getFieldsList();

			for(DataField field : fields)
			{
				if(field.isAutoIncrement())
					createIdGenerator(field, nodeMetadata);
			}
		}
	}

}
