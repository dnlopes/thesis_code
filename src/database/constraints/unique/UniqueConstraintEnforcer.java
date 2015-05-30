package database.constraints.unique;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import nodes.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.defaults.Configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by dnlopes on 25/03/15.
 */
public class UniqueConstraintEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(UniqueConstraintEnforcer.class);

	private Set<String> currentValues;
	private List<DataField> fields;
	private UniqueConstraint constraint;
	private String tableName;

	public UniqueConstraintEnforcer(List<DataField> field, NodeConfig config, UniqueConstraint constraint)
	{
		this.fields = field;
		this.tableName = field.get(0).getTableName();
		this.currentValues = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.constraint = constraint;
		this.setup(config);
	}

	private void setup(NodeConfig config)
	{
		LOG.trace("scanning all used values");

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(this.getQueryClause());
		buffer.append(" FROM ");
		buffer.append(this.fields.get(0).getTableName());

		try
		{
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config.getDbProps(), Configuration
					.getInstance().getDatabaseName());
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			while(rs.next())
			{
				StringBuilder pkBuffer = new StringBuilder();

				for(int i = 0; i < this.fields.size(); i++)
				{
					if(i == 0)
						pkBuffer.append(rs.getObject(i + 1).toString());
					else
					{
						pkBuffer.append(",");
						pkBuffer.append(rs.getObject(i + 1).toString());
					}
				}

				String uniqueValue = pkBuffer.toString();
				if(!this.reservValue(uniqueValue))
				{
					LOG.error("duplicated values for constraint for constraint {}",
							this.constraint.getConstraintIdentifier());
					RuntimeUtils.throwRunTimeException("duplicated values for constraint for constraint",
							ExitCode.HASHMAPDUPLICATE);
				}
			}

			rs.close();
			stmt.close();
			tempConnection.close();
		} catch(SQLException e)
		{
			LOG.error("error while fetching all used values for constraint {}",
					this.constraint.getConstraintIdentifier(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.FETCH_RESULTS_ERROR);
		}

		LOG.trace("{} values already in use for constraint {}", this.currentValues.size(),
				this.constraint.getConstraintIdentifier());
	}

	public boolean reservValue(String newValue)
	{
		return this.currentValues.add(newValue);
	}

	private String getQueryClause()
	{
		StringBuilder buffer = new StringBuilder();

		Iterator<DataField> it = this.fields.iterator();

		while(it.hasNext())
		{
			buffer.append(it.next().getFieldName());
			if(it.hasNext())
				buffer.append(",");
		}

		return buffer.toString();
	}

	public String getTableName()
	{
		return this.tableName;
	}
}
