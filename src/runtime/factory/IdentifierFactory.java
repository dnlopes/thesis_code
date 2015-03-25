package runtime.factory;


import database.util.DataField;
import network.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.IDGenerator;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 */
public class IdentifierFactory
{

	private static final Logger LOG = LoggerFactory.getLogger(IdentifierFactory.class);
	private static final Map<DataField, IDGenerator> ID_GENERATORS = new HashMap<>();

	public static void createIdGenerator(DataField field, NodeMetadata nodeMetadata)
	{
		IDGenerator newGenerator = new IDGenerator(field, nodeMetadata);
		ID_GENERATORS.put(field, newGenerator);
		LOG.info("id generator for field {} created. Initial value {}", field.getFieldName(),
				newGenerator.getCurrentValue());
	}

	public static int getNextId(DataField field)
	{
		if(!ID_GENERATORS.containsKey(field))
			RuntimeHelper.throwRunTimeException("id generator not found", ExitCode.ID_GENERATOR_ERROR);

		return ID_GENERATORS.get(field).getNextId();
	}

}
