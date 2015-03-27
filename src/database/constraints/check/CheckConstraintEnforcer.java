package database.constraints.check;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import network.coordinator.CoordinatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;
import util.defaults.ScratchpadDefaults;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 */
public class CheckConstraintEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(CheckConstraintEnforcer.class);

	private CheckConstraint checkConstraint;
	private DataField field;
	// stores rowId, value
	private Map<Integer, Double> currentValues;

	public CheckConstraintEnforcer(DataField field, CheckConstraint constraint, CoordinatorConfig config)
	{
		this.checkConstraint = constraint;
		this.currentValues = new HashMap<>();
		this.field = field;
		this.setup(config);
	}

	private void setup(CoordinatorConfig config)
	{
		LOG.trace("scanning all used values");

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
		buffer.append(",");
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
				int rowId = rs.getInt(ScratchpadDefaults.SCRATCHPAD_COL_IMMUTABLE);
				String currentValue = rs.getString(field.getFieldName());
				currentValues.put(rowId, Double.parseDouble(currentValue));
			}

			rs.close();
			stmt.close();
			tempConnection.close();
		} catch(SQLException e)
		{
			LOG.error("error while fetching all current values for field {}", this.field.getFieldName(), e);
			RuntimeHelper.throwRunTimeException(e.getMessage(), ExitCode.FETCH_RESULTS_ERROR);
		}

		LOG.trace("{} values inserted", this.currentValues.size());
	}

	public boolean applyDelta(int rowId, String delta)
	{
		double currentValue = this.currentValues.get(rowId);
		double finalValue = currentValue + Double.parseDouble(delta);

		if(this.checkConstraint.isValidValue(String.valueOf(finalValue)))
		{
			this.currentValues.put(rowId, finalValue);
			return true;
		} else
			return false;
	}
}
