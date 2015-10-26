package runtime;


import database.constraints.unique.AutoIncrementConstraint;
import database.constraints.unique.UniqueConstraint;
import database.jdbc.ConnectionFactory;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import nodes.NodeConfig;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.Configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 24/03/15.
 */
public class IDsManager
{

	private static final Logger LOG = LoggerFactory.getLogger(IDsManager.class);

	private Map<String, IDGenerator> idsGenerators;
	private Map<String, StringGenerator> stringGenerators;
	private static String PREFIX;

	public IDsManager(String prefix, NodeConfig replicatorConfig)
	{
		PREFIX = prefix;
		this.idsGenerators = new HashMap<>();
		this.stringGenerators = new HashMap<>();

		setup(replicatorConfig);
	}

	public String getNextString(String tableName, String fieldName)
	{
		String key = tableName + "_" + fieldName;

		if(!this.stringGenerators.containsKey(key))
			RuntimeUtils.throwRunTimeException("id generator not found for key " + key, ExitCode.ID_GENERATOR_ERROR);

		String nextString = this.stringGenerators.get(key).getNextString();

		if(LOG.isTraceEnabled())
			LOG.trace("new unique string generated for key {}: {}", key, nextString);

		return nextString;
	}

	public int getNextId(String tableName, String fieldName)
	{
		String key = tableName + "_" + fieldName;

		if(!this.idsGenerators.containsKey(key))
			RuntimeUtils.throwRunTimeException("id generator not found for key " + key, ExitCode.ID_GENERATOR_ERROR);

		int nextId = this.idsGenerators.get(key).getNextId();

		if(LOG.isTraceEnabled())
			LOG.trace("new unique id generated for key {}: {}", key, nextId);

		return nextId;
	}

	public static String appendReplicaPrefix(String value)
	{
		StringBuilder buffer = new StringBuilder("'");
		buffer.append(PREFIX);
		buffer.append(StringUtils.substring(value, 1, value.length() - 1));
		buffer.append("'");

		return buffer.toString();
	}

	private void setup(NodeConfig config)
	{
		if(LOG.isTraceEnabled())
			LOG.trace("bootstraping id generators for auto increment fields");

		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			/*
			for(UniqueConstraint uniqueConstraint : table.getUniqueConstraints())
			{
				if(!uniqueConstraint.requiresCoordination())
				{
					DataField toChangeField = uniqueConstraint.getFieldToChange();
					if(toChangeField == null)
						RuntimeUtils.throwRunTimeException(
								"unique constraint without coordination must have " + "toChangeField set",
								ExitCode.NULLPOINTER);

					if(toChangeField.isNumberField())
						createIdGenerator(toChangeField, config);
					else if(toChangeField.isStringField())
						createStringGenerator(toChangeField, config);

				}
			}           */

			for(AutoIncrementConstraint autoIncrementConstraint : table.getAutoIncrementConstraints())
				if(!autoIncrementConstraint.requiresCoordination())
					createIdGenerator(autoIncrementConstraint.getAutoIncrementField(), config);
		}
	}

	private void createStringGenerator(DataField field, NodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(this.stringGenerators.containsKey(key))
		{
			if(LOG.isWarnEnabled())
				LOG.warn("string generator already created. Silently ignored");
			return;
		}

		StringGenerator newGenerator = new StringGenerator(field);

		this.stringGenerators.put(key, newGenerator);

	}

	private void createIdGenerator(DataField field, NodeConfig config)
	{
		String key = field.getTableName() + "_" + field.getFieldName();

		if(this.idsGenerators.containsKey(key))
		{
			if(LOG.isWarnEnabled())
				LOG.warn("ids generator already created. Silently ignored");
			return;
		}

		IDGenerator newGenerator = new IDGenerator(field, config, Configuration.getInstance().getReplicatorsCount());

		this.idsGenerators.put(key, newGenerator);

		if(LOG.isTraceEnabled())
			LOG.trace("id generator for field {} created. Initial value {}", field.getFieldName(),
					newGenerator.getCurrentValue());
	}

	private class IDGenerator
	{

		private final int delta;
		private AtomicInteger currentValue;
		private DataField field;

		public IDGenerator(DataField field, NodeConfig config, int delta)
		{
			this.field = field;
			this.currentValue = new AtomicInteger();
			this.delta = delta;

			this.setupGenerator(config);
		}

		private void setupGenerator(NodeConfig config)
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("SELECT MAX(");
			buffer.append(this.field.getFieldName());
			buffer.append(") AS ");
			buffer.append(this.field.getFieldName());
			buffer.append(" FROM ");
			buffer.append(this.field.getTableName());

			Connection tempConnection = null;
			Statement stmt = null;
			ResultSet rs = null;
			try
			{
				tempConnection = ConnectionFactory.getDefaultConnection(config);
				stmt = tempConnection.createStatement();
				rs = stmt.executeQuery(buffer.toString());

				if(rs.next())
				{
					int lastId = rs.getInt(this.field.getFieldName());
					this.currentValue.set(lastId + config.getId());
				} else
				{
					LOG.error("could not fetch the last id for field {}", this.field.getFieldName());
					RuntimeUtils.throwRunTimeException("id generator failed to initialize properly",
							ExitCode.ID_GENERATOR_ERROR);
				}
			} catch(SQLException e)
			{
				LOG.error("could not fetch the last id for field {}. Reason: {}", this.field.getFieldName(),
						e.getMessage());
				RuntimeUtils.throwRunTimeException("id generator failed to initialize properly",
						ExitCode.ID_GENERATOR_ERROR);
			} finally
			{
				DbUtils.closeQuietly(tempConnection, stmt, rs);
			}
		}

		public int getNextId()
		{
			int newValue = this.currentValue.addAndGet(this.delta);
			LOG.debug("new id generated for field {}: {}", this.field.getFieldName(), newValue);

			return newValue;
		}

		public int getCurrentValue()
		{
			return this.currentValue.get();
		}
	}


	private class StringGenerator
	{

		private AtomicInteger currentValue;
		private DataField field;

		public StringGenerator(DataField field)
		{
			this.field = field;
			this.currentValue = new AtomicInteger();
		}

		public String getNextString()
		{
			int newValue = this.currentValue.incrementAndGet();
			String uniqueString = PREFIX + String.valueOf(newValue);

			LOG.debug("new string generated for field {}: {}", this.field.getFieldName(), uniqueString);

			return uniqueString;
		}
	}
}
