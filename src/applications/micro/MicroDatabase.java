package applications.micro;


import applications.micro.workload.MicroConstants;
import database.jdbc.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.props.DatabaseProperties;

import java.sql.*;
import java.util.Random;


public class MicroDatabase implements MicroConstants
{

	static final Logger LOG = LoggerFactory.getLogger(MicroDatabase.class);

	protected Connection conn;
	protected Statement stat;

	final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private Random randomGenerator;

	public MicroDatabase(DatabaseProperties props) throws SQLException, ClassNotFoundException
	{
		this.randomGenerator = new Random(System.currentTimeMillis());
		this.conn = ConnectionFactory.getDefaultConnection(props, "micro");
		this.conn.setAutoCommit(false);
		this.stat = conn.createStatement();
	}

	private String getRandomString(int length)
	{
		StringBuilder rndString = new StringBuilder(length);
		for(int i = 0; i < length; i++)
		{
			rndString.append(charSet.charAt(randomGenerator.nextInt(charSet.length())));
		}
		return rndString.toString();
	}

	public void setupDatabase(boolean useForeignKeys) throws SQLException
	{
		this.createDB();
		this.createTables(useForeignKeys);
		LOG.info("Micro database tables created");
		this.insertIntoTables();
		LOG.info("Micro database tables populated");
		this.createClockFunction();
		LOG.info("User Functions created");
		LOG.info("Micro database is ready");
	}

	private void createDB()
	{
		try
		{
			stat.execute("DROP DATABASE IF EXISTS micro");
			conn.commit();
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		try
		{
			stat.execute("CREATE DATABASE micro");
		} catch(SQLException e)
		{

			e.printStackTrace();
		}

		try
		{
			stat.execute("use micro;");
		} catch(SQLException e)
		{

			e.printStackTrace();
		}
		try
		{
			conn.commit();
		} catch(SQLException e)
		{

			e.printStackTrace();
		}

	}

	private void createTables(boolean useForeignKeys) throws SQLException
	{
		int dumb = 0;
		for(int i = 1; i <= MicroConstants.NUMBER_OF_TABLES; i++)
		{
			stat.execute("DROP TABLE IF EXISTS t" + i);
			conn.commit();

			String statement;

			if(i == 0)
				statement = "CREATE TABLE t" + i + " (" +
						"a int(10) NOT NULL, " +
						"b int(10), " +
						"c int(10), " +
						"d int(10), " +
						"e varchar(50) NOT NULL, " +
						"_del BOOLEAN NOT NULL DEFAULT 0, " +
						"_cclock varchar(20) DEFAULT '0', " +
						"_dclock varchar(20) DEFAULT '0', " +
						"PRIMARY KEY(a)" +
						");";
			else if(useForeignKeys)
				statement = "CREATE TABLE t" + i + " (" +
						"a int(10) NOT NULL, " +
						"b int(10), " +
						"c int(10), " +
						"d int(10), " +
						"e varchar(50) NOT NULL, " +
						"_del BOOLEAN NOT NULL DEFAULT 0, " +
						"_cclock varchar(20) DEFAULT '0', " +
						"_dclock varchar(20) DEFAULT '0', " +
						"PRIMARY KEY(a), " +
						"FOREIGN KEY (b) REFERENCES t" + dumb + "(a) ON DELETE CASCADE" +
						");";
			else
				statement = "CREATE TABLE t" + i + " (" +
						"a int(10) NOT NULL, " +
						"b int(10), " +
						"c int(10), " +
						"d int(10), " +
						"e varchar(50) NOT NULL, " +
						"_del BOOLEAN NOT NULL DEFAULT 0, " +
						"_cclock varchar(20) DEFAULT '0', " +
						"_dclock varchar(20) DEFAULT '0', " +
						"PRIMARY KEY(a)" +
						");";

			stat.execute(statement);
			conn.commit();
		}
		conn.commit();
	}

	private void insertIntoTables() throws SQLException
	{
		for(int i = 1; i <= MicroConstants.NUMBER_OF_TABLES; i++)
		{
			for(int j = 0; j < MicroConstants.RECORDS_PER_TABLE; j++)
			{
				int a = j;
				int b = randomGenerator.nextInt(MicroConstants.RECORDS_PER_TABLE) - 1;
				if(i == 1 || i == 2)
					b = 0;

				int c = randomGenerator.nextInt(MicroConstants.RECORDS_PER_TABLE) - 1;
				int d = randomGenerator.nextInt(MicroConstants.RECORDS_PER_TABLE) - 1;
				String e = getRandomString(5);

				String statement = "insert into t" + i + " values (" + Integer.toString(a) + "," + Integer.toString(
						b) + "," +
						Integer.toString(c) + "," + Integer.toString(d) + ",'" + e + "', 0,'0','0')";
				stat.execute(statement);
			}
			conn.commit();
		}
		conn.commit();
	}

	private void createClockFunction() throws SQLException
	{
		String proc = "CREATE FUNCTION testClock(currentClock CHAR(100), newClock CHAR(100)) RETURNS int BEGIN DECLARE " +
				"isConcurrent BOOL; DECLARE isLesser BOOL; DECLARE isGreater BOOL; DECLARE returnValue INT; SET " +
				"@returnValue = 0; SET @isConcurrent = FALSE; SET @isLesser = FALSE; SET @isGreater = FALSE; loopTag: " +
				"WHILE (TRUE) DO SET @currEntry = CONVERT ( LEFT(currentClock, 1), SIGNED); SET @newEntry = CONVERT ( " +
				"LEFT(newClock, 1), SIGNED); IF(@currEntry > @newEntry) then IF(@isLesser) then SET @isConcurrent = " +
				"TRUE; SET @isLesser = FALSE; SET @isGreater = FALSE; LEAVE loopTag; END IF; SET @isGreater = TRUE; " +
				"END IF; IF(@currEntry < @newEntry) then IF(@isGreater) then SET @isConcurrent = TRUE; SET @isLesser =" +
				" FALSE; SET @isGreater = FALSE; LEAVE loopTag; END IF; SET @isLesser = TRUE; END IF; IF (LENGTH" +
				"(currentClock) = 1) then LEAVE loopTag; END IF; SET currentClock = SUBSTRING(currentClock, LOCATE" +
				"('-', currentClock) + 1); SET newClock = SUBSTRING(newClock, LOCATE('-', newClock) + 1); END WHILE; " +
				"IF(@isLesser) then SELECT 1 INTO @returnValue; /* SELECT 'First clock is LESSER then second' as " +
				"'Message';*/ ELSEIF(@isGreater) then SELECT 2 INTO @returnValue; /* SELECT 'First clock is GREATER " +
				"then second' as 'Message';*/ ELSEIF(@isConcurrent) then SELECT 3 INTO @returnValue; /* SELECT 'Clocks" +
				" are concurrent' as 'Message';*/ ELSE SELECT 4 INTO @returnValue; /* SELECT 'Clocks are equal' as " +
				"'Message';*/ END IF; RETURN @returnValue; END;";

		stat.execute(proc);
	}
}
