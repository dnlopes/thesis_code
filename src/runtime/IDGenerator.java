package runtime;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import nodes.NodeConfig;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ExitCode;
import util.defaults.Configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 24/03/15.
 */
public class IDGenerator
{

	private static final Logger LOG = LoggerFactory.getLogger(IDGenerator.class);

	private AtomicInteger currentValue;
	private DataField field;
	private int delta;

	public IDGenerator(DataField field, NodeConfig config)
	{
		this.field = field;
		this.currentValue = new AtomicInteger();
		this.delta = Configuration.getInstance().getProxies().size();

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
				this.currentValue.set(lastId);
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
		return this.currentValue.addAndGet(this.delta);
	}

	public int getCurrentValue()
	{
		return this.currentValue.get();
	}
}
