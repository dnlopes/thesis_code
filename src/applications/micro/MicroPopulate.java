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

	public static void main(String[] argv)
	{
		if(argv.length != 1)
		{
			System.out.println("usage: java -jar micro-gendb.jar <database host>");
			System.exit(1);
		}

		String dbHost = argv[0];

		DatabaseProperties dbProps = new DatabaseProperties("sa", "101010", dbHost, 3306);
		MicroPopulate dbPopulate = new MicroPopulate(dbProps);

		dbPopulate.setupDatabase();
	}

	public MicroPopulate(DatabaseProperties dbProps)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			StringBuilder buffer = new StringBuilder("jdbc:mysql://");
			buffer.append(dbProps.getDbHost());
			buffer.append(":");
			buffer.append(dbProps.getDbPort());

			this.connection = DriverManager.getConnection(buffer.toString(), dbProps.getDbUser(), dbProps.getDbPwd());
			this.connection.setAutoCommit(false);

		} catch(ClassNotFoundException | SQLException e)
		{
			LOG.error("database population failed: {}", e.getMessage(), e);
			System.exit(-1);
		}
	}

	public void setupDatabase()
	{
		//this.createDB();
		this.createTables();
		//this.createClockFunction();
		this.populateDatabase();
	}

	private void populateDatabase()
	{
		LOG.info("starting database population");
		Statement stat = null;
		try
		{
			stat = this.connection.createStatement();
			stat.execute("use micro;");
			this.connection.commit();

			for(int i = 1; i <= NUMBER_OF_TABLES; i++)
			{
				LOG.info("population table t{}", i);

				for(int j = 0; j < RECORDS_PER_TABLE; j++)
				{
					int b = GeneratorUtils.randomNumber(0, 5000);
					int c = GeneratorUtils.randomNumber(0, 5000);
					int d = GeneratorUtils.randomNumber(0, 5000);
					String e = GeneratorUtils.makeAlphaString(5, 10);
					StringBuilder buffer = new StringBuilder("INSERT INTO t");
					buffer.append(i);
					buffer.append(" (a,b,c,d,e) VALUES(");
					buffer.append(j);
					buffer.append(",");
					buffer.append(j);
					buffer.append(",");
					buffer.append(c);
					buffer.append(",");
					buffer.append(d);
					buffer.append(",");
					buffer.append("'");
					buffer.append(e);
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
			this.connection.commit();
			stat.close();
			this.connection.close();
		} catch(SQLException e)
		{
			LOG.error("failed to close database resources: {}", e.getMessage(), e);
		}
	}

	private void createDB()
	{
		Statement stat = null;

		try
		{
			stat = this.connection.createStatement();
			LOG.info("dropping micro database");
			stat.execute("DROP DATABASE IF EXISTS micro");
			this.connection.commit();
			LOG.info("creating micro database");
			stat.execute("CREATE DATABASE micro");
			stat.execute("use micro;");
			this.connection.commit();

		} catch(SQLException e)
		{
			LOG.error("failed to create database: {}", e.getMessage(), e);
			System.exit(-1);
		}
	}

	private void createTables()
	{
		Statement stat;
		try
		{
			stat = this.connection.createStatement();
			stat.execute("use micro;");

			for(int i = 1; i <= MicroConstants.NUMBER_OF_TABLES; i++)
			{
				stat.execute("DROP TABLE IF EXISTS t" + i);

				this.connection.commit();

				String statement = "CREATE TABLE t" + i + " (" +
						"a int(10) NOT NULL, " +
						"b int(10), " +
						"c int(10), " +
						"d int(10), " +
						"e varchar(50) NOT NULL, " +
						"_del BOOLEAN NOT NULL DEFAULT 0, " +
						"_cclock varchar(50), " +
						"_dclock varchar(50), " +
						"PRIMARY KEY(a)," +
						"UNIQUE (b)" +
						");";

				stat.execute(statement);
				this.connection.commit();
			}
			this.connection.commit();
		} catch(SQLException e)
		{
			LOG.error("failed to create database tables: {}", e.getMessage(), e);
			System.exit(-1);
		}
	}

	private void createClockFunction()
	{
		Statement stat;
		try
		{
			stat = this.connection.createStatement();

			String proc = "DROP FUNCTION IF EXISTS compareClocks; DELIMITER // CREATE FUNCTION compareClocks" +
					"(currentClock CHAR(100), newClock CHAR(100)) RETURNS int BEGIN DECLARE isConcurrent BOOL; DECLARE" +
					" isLesser BOOL; DECLARE dumbFlag BOOL; DECLARE isGreater BOOL; DECLARE cycleCond BOOL; DECLARE " +
					"returnValue INT; SET @dumbFlag = FALSE; SET @returnValue = 0; SET @isConcurrent = FALSE; SET " +
					"@isLesser = FALSE; SET @isGreater = FALSE; IF(currentClock IS NULL) then RETURN 1; END IF; " +
					"loopTag: WHILE (TRUE) DO SET @currEntry = CONVERT ( LEFT(currentClock, 1), SIGNED); SET @newEntry" +
					" = CONVERT ( LEFT(newClock, 1), SIGNED); IF(@currEntry > @newEntry) then SET @dumbFlag = TRUE; IF" +
					"(@isLesser) then SET @isConcurrent = TRUE; LEAVE loopTag; END IF; SET @isGreater = TRUE; ELSEIF" +
					"(@currEntry < @newEntry) then IF(@isGreater) then SET @isConcurrent = TRUE; IF(@dumbFlag = FALSE)" +
					" then SET @isGreater = TRUE; END IF; LEAVE loopTag; END IF; SET @isLesser = TRUE; END IF; IF " +
					"(LENGTH(currentClock) = 1) then LEAVE loopTag; END IF; SET currentClock = SUBSTRING(currentClock," +
					" LOCATE('-', currentClock) + 1); SET newClock = SUBSTRING(newClock, LOCATE('-', newClock) + 1); " +
					"END WHILE; IF(@isConcurrent AND @dumbFlag = FALSE) then SELECT 0 INTO @returnValue; /* SELECT " +
					"'Clocks are concurrent' as 'Message';*/ ELSEIF(@isLesser) then SELECT 1 INTO @returnValue; /* " +
					"SELECT 'Second clock is GREATER then second' as 'Message';*/ ELSE SELECT -1 INTO @returnValue; /*" +
					" SELECT 'Second clock is LESSER then second' as 'Message';*/ END IF; RETURN @returnValue; END // " +
					"DELIMITER ; ";

			stat.execute(proc);
			this.connection.commit();

		} catch(SQLException e)
		{
			LOG.error("failed to create clockFunction: {}", e.getMessage(), e);
			System.exit(-1);
		}
	}
}
