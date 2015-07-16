package database.constraints.unique;


import database.jdbc.ConnectionFactory;
import database.util.field.DataField;
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
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 25/03/15.
 */
public class AutoIncrementEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(AutoIncrementEnforcer.class);

	private AtomicInteger currentId;
	private DataField field;
	private AutoIncrementConstraint constraint;


	public AutoIncrementEnforcer(DataField field, NodeConfig config, AutoIncrementConstraint constraint)
	{
		this.field = field;
		this.constraint = constraint;
		this.setup(config);
	}

	private void setup(NodeConfig config)
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
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config.getDbProps(), Configuration
					.getInstance().getDatabaseName());
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			if(rs.next())
			{
				this.currentId = new AtomicInteger(rs.getInt(this.field.getFieldName()));
				rs.close();
				stmt.close();
				tempConnection.close();
			} else
			{
				rs.close();
				stmt.close();
				tempConnection.close();
				LOG.error("could not fetch the last id for field {}", this.field.getFieldName());
				RuntimeUtils.throwRunTimeException("id generator failed to initialize properly",
						ExitCode.ID_GENERATOR_ERROR);
			}
		} catch(SQLException e)
		{
			LOG.error("could not fetch the last id for constraint {}", this.constraint.getConstraintIdentifier(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.ID_GENERATOR_ERROR);
		}

		if(LOG.isTraceEnabled())
			LOG.trace("current id for field {} is {}", this.field.getFieldName(), this.currentId);
	}

	public int getNextId()
	{
		return this.currentId.incrementAndGet();
	}
}

