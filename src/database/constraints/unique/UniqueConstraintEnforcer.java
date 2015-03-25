package database.constraints.unique;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import network.NodeMetadata;
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

	public UniqueConstraintEnforcer(DataField field, NodeMetadata nodeMetadata)
	{
		this.field = field;
		this.currentValues = new HashSet<>();
		this.setup(nodeMetadata);
	}

	private void setup(NodeMetadata nodeMetadata)
	{
		LOG.trace("scanning all used values");

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(field.getFieldName());
		buffer.append(" FROM ");
		buffer.append(this.field.getTableName());

		try
		{
			Connection tempConnection = ConnectionFactory.getDefaultConnection(nodeMetadata);
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
			LOG.error("error while fetching all used values for field {}. Reason: {}", this.field.getFieldName(),
					e.getMessage());
			e.printStackTrace();
			RuntimeHelper.throwRunTimeException("error while fetching values from main database",
					ExitCode.FETCH_RESULTS_ERROR);
		}

		LOG.trace("{} values already in use", this.currentValues.size());
	}

	public boolean reserveValue(String newValue)
	{
		return this.currentValues.add(newValue);
	}
}
