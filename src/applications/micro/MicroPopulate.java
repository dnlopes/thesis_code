package applications.micro;


import applications.micro.workload.MicroConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.props.DatabaseProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroPopulate implements MicroConstants
{

	private static final Logger LOG = LoggerFactory.getLogger(MicroPopulate.class);
	private Connection connection;

	public MicroPopulate(DatabaseProperties dbProps)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			StringBuilder buffer = new StringBuilder("jdbc:mysql://");
			buffer.append(dbProps.getDbHost());
			buffer.append(":");
			buffer.append(dbProps.getDbPort());
			buffer.append("/");
			buffer.append("micro");

			this.connection = DriverManager.getConnection(buffer.toString(), dbProps.getDbUser(), dbProps.getDbPwd());
			this.connection.setAutoCommit(false);

		} catch(ClassNotFoundException | SQLException e)
		{
			LOG.error("database population failed: {}", e.getMessage(), e);
			System.exit(-1);
		}
	}

	public void populateDatabase()
	{
		LOG.info("starting database population");
		Statement stat = null;
		try
		{
			stat = this.connection.createStatement();

			for(int i = 0; i < NUMBER_OF_TABLES; i++)
			{
				LOG.info("population table t{}", i);

				for(int j = 0; j < RECORDS_PER_TABLE; j++)
				{
					int b = GeneratorUtils.randomNumber(0, 5000);
					int c = GeneratorUtils.randomNumber(0, 5000);
					String d = GeneratorUtils.makeAlphaString(5, 10);
					StringBuilder buffer = new StringBuilder("INSERT INTO t");
					buffer.append(i);
					buffer.append(" VALUES(");
					buffer.append(j);
					buffer.append(",");
					buffer.append(b);
					buffer.append(",");
					buffer.append(c);
					buffer.append(",");
					buffer.append("'");
					buffer.append(d);
					buffer.append("'");
					buffer.append(")");

					String sqlOp = buffer.toString();
					if(LOG.isTraceEnabled())
						LOG.trace(sqlOp);

					stat.execute(sqlOp);
				}
			}

		} catch(SQLException e)
		{
			LOG.error("database population failed: {}", e.getMessage(), e);
			System.exit(-1);
		}

		try
		{
			stat.close();
			this.connection.close();
		} catch(SQLException e)
		{
			LOG.error("failed to close database resources: {}", e.getMessage(), e);
		}
	}
}
