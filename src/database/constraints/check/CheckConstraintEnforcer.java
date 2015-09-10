package database.constraints.check;


import database.jdbc.ConnectionFactory;
import database.util.*;
import database.util.field.DataField;
import database.util.table.DatabaseTable;
import nodes.NodeConfig;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeUtils;
import util.ExitCode;
import util.Configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import java.util.Map;


/**
 * Created by dnlopes on 24/03/15.
 * @DEPRECATED
 */
public class CheckConstraintEnforcer
{

	static final Logger LOG = LoggerFactory.getLogger(CheckConstraintEnforcer.class);

	private CheckConstraint checkConstraint;
	private DataField field;
	private DatabaseTable dbTable;
	// stores rowId, value
	private Map<String, Double> currentValues;

	public CheckConstraintEnforcer(DataField field, CheckConstraint constraint, NodeConfig config)
	{
		this.checkConstraint = constraint;
		this.currentValues = new HashMap<>();
		this.field = field;
		this.dbTable = Configuration.getInstance().getDatabaseMetadata().getTable(field.getTableName());
		this.setup(config);
	}

	private void setup(NodeConfig config)
	{
		if(LOG.isTraceEnabled())
			LOG.trace("scanning all used values");

		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT ");
		buffer.append(this.dbTable.getPrimaryKey().getQueryClause());
		buffer.append(",");
		buffer.append(field.getFieldName());
		buffer.append(" FROM ");
		buffer.append(this.field.getTableName());

		try
		{
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config.getDbProps(), Configuration
					.getInstance().getDatabaseName());
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			while(rs.next())
			{
				PrimaryKeyValue pkValue = DatabaseCommon.getPrimaryKeyValue(rs, this.dbTable);
				String currentValue = rs.getString(field.getFieldName());
				currentValues.put(pkValue.getUniqueValue(), Double.parseDouble(currentValue));
			}

			DbUtils.closeQuietly(tempConnection, stmt, rs);
		} catch(SQLException e)
		{
			LOG.error("error while fetching all current values for field {}", this.field.getFieldName(), e);
			RuntimeUtils.throwRunTimeException(e.getMessage(), ExitCode.FETCH_RESULTS_ERROR);
		}

		if(LOG.isTraceEnabled())
			LOG.trace("{} values inserted", this.currentValues.size());
	}

	public boolean applyDelta(String id, String delta)
	{
		double currentValue, finalValue;

		if(!this.currentValues.containsKey(id))
			finalValue = Double.parseDouble(delta);
		else
		{
			currentValue = this.currentValues.get(id);
			finalValue = currentValue + Double.parseDouble(delta);
		}

		if(this.checkConstraint.isValidValue(String.valueOf(finalValue)))
		{
			this.currentValues.put(id, finalValue);
			return true;
		} else
			return false;
	}
}
