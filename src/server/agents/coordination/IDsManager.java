package server.agents.coordination;


import common.database.constraints.unique.AutoIncrementConstraint;
import common.database.field.DataField;
import common.database.table.DatabaseTable;
import common.nodes.NodeConfig;
import common.util.ConnectionFactory;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import common.util.RuntimeUtils;
import common.util.ExitCode;
import common.Configuration;

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
	private static String PREFIX;

	private Map<String, IDGenerator> idsGenerators;

	public IDsManager(String prefix, NodeConfig replicatorConfig)
	{
		PREFIX = prefix;
		this.idsGenerators = new HashMap<>();

		setup(replicatorConfig);
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

	private void setup(NodeConfig config)
	{
		if(LOG.isTraceEnabled())
			LOG.trace("bootstraping id generators for auto increment fields");

		for(DatabaseTable table : Configuration.getInstance().getDatabaseMetadata().getAllTables())
		{
			for(AutoIncrementConstraint autoIncrementConstraint : table.getAutoIncrementConstraints())
				if(!autoIncrementConstraint.requiresCoordination())
					createIdGenerator(autoIncrementConstraint.getAutoIncrementField(), config);
		}
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
