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


/**
 * Created by dnlopes on 25/03/15.
 */
public class AutoIncrementEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(AutoIncrementEnforcer.class);

	private int currentId;
	private DataField field;

	public AutoIncrementEnforcer(DataField field, CoordinatorConfig config)
	{
		this.field = field;
		this.setup(config);
	}

	private void setup(CoordinatorConfig config)
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT MAX(");
		buffer.append(this.field.getFieldName());
		buffer.append(") AS ");
		buffer.append(this.field.getFieldName());
		buffer.append(" FROM ");
		buffer.append(this.field.getTableName());

		try
		{
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config.getReplicatorConfig());
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			if(rs.next())
			{
				this.currentId = rs.getInt(this.field.getFieldName());
				rs.close();
				stmt.close();
				tempConnection.close();
			} else
			{
				rs.close();
				stmt.close();
				tempConnection.close();
				LOG.error("could not fetch the last id for field {}", this.field.getFieldName());
				RuntimeHelper.throwRunTimeException("id generator failed to initialize properly",
						ExitCode.ID_GENERATOR_ERROR);
			}
		} catch(SQLException e)
		{
			LOG.error("could not fetch the last id for field {}", this.field.getFieldName(), e);
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.ID_GENERATOR_ERROR);
		}

		LOG.info("current id for field {} is {}", this.field.getFieldName(), this.currentId);
	}

	public int getNextId()
	{
		return ++currentId;
	}
}

