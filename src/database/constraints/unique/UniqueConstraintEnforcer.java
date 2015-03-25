package database.constraints.unique;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import network.coordinator.CoordinatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by dnlopes on 25/03/15.
 */
public class UniqueConstraintEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(UniqueConstraintEnforcer.class);

	private Set<String> currentValues;
	private DataField field;

	public UniqueConstraintEnforcer(DataField field, CoordinatorConfig config)
	{
		this.field = field;
		this.currentValues = new HashSet<>();
		this.setup(config);
	}

	private void setup(CoordinatorConfig config)
	{
		LOG.trace("scanning all used values");

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(field.getFieldName());
		buffer.append(" FROM ");
		buffer.append(this.field.getTableName());

		try
		{
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config.getReplicatorConfig());
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			while(rs.next())
			{
				String uniqueValue = rs.getString(this.field.getFieldName());
				this.reserveValue(uniqueValue);
			}

			rs.close();
			stmt.close();

		} catch(SQLException e)
		{
			LOG.error("error while fetching all used values for field {}", this.field.getFieldName(), e);
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.FETCH_RESULTS_ERROR);
		}

		LOG.trace("{} values already in use", this.currentValues.size());
	}

	public boolean reserveValue(String newValue)
	{
		return this.currentValues.add(newValue);
	}
}
