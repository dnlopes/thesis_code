package database.constraints.check;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import network.NodeMetadata;
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

	public CheckConstraintEnforcer(DataField field, CheckConstraint constraint, NodeMetadata nodeMetadata)
	{
		this.checkConstraint = constraint;
		this.currentValues = new HashMap<>();
		this.field = field;
		this.setup(nodeMetadata);
	}

	private void setup(NodeMetadata nodeMetadata)
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
			Connection tempConnection = ConnectionFactory.getDefaultConnection(nodeMetadata);
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

		} catch(SQLException e)
		{
			LOG.error("error while fetching all current values for field {}. Reason: {}", this.field.getFieldName(),
					e.getMessage());
			e.printStackTrace();
			RuntimeHelper.throwRunTimeException("error while fetching values from main database",
					ExitCode.FETCH_RESULTS_ERROR);
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
