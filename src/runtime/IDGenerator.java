package runtime;


import database.jdbc.ConnectionFactory;
import database.util.DataField;
import network.proxy.ProxyConfig;
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


	public IDGenerator(DataField field, ProxyConfig config)
	{
		this.field = field;
		this.currentValue = new AtomicInteger();
		this.delta = Configuration.getInstance().getProxies().size();

		this.setupGenerator(config);
	}

	private void setupGenerator(ProxyConfig config)
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
			Connection tempConnection = ConnectionFactory.getDefaultConnection(config);
			Statement stmt = tempConnection.createStatement();
			ResultSet rs = stmt.executeQuery(buffer.toString());

			if(rs.next())
			{
				int lastId = rs.getInt(this.field.getFieldName());
				this.currentValue.set(lastId + config.getId());

				rs.close();
				stmt.close();
			} else
			{
				LOG.error("could not fetch the last id for field {}", this.field.getFieldName());
				RuntimeHelper.throwRunTimeException("id generator failed to initialize properly",
						ExitCode.ID_GENERATOR_ERROR);
			}
		} catch(SQLException e)
		{
			LOG.error("could not fetch the last id for field {}. Reason: {}", this.field.getFieldName(),
					e.getMessage());
			e.printStackTrace();
			RuntimeHelper.throwRunTimeException("id generator failed to initialize properly",
					ExitCode.ID_GENERATOR_ERROR);
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
