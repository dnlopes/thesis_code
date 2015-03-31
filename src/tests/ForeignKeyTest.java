package tests;


import database.jdbc.ConnectionFactory;
import util.defaults.DBDefaults;
import util.props.DatabaseProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by dnlopes on 28/03/15.
 */
public class ForeignKeyTest
{

	public static void main(String args[]) throws SQLException, ClassNotFoundException
	{

		DatabaseProperties dbProperties = new DatabaseProperties(DBDefaults.DATABASE_PROPERTIES_FILE, true);
		Connection connection = ConnectionFactory.getDefaultConnection(dbProperties);

		connection.setAutoCommit(false);
		Statement stat = connection.createStatement();
		stat.execute("DROP DATABASE if exists test");
		connection.commit();
		stat.execute("create database test");
		connection.commit();
		stat.execute("use test");
		String statement = "CREATE TABLE t1(" +
				"a int(10) NOT NULL, " +
				"b int(10) NOT NULL," +
				"PRIMARY KEY(a), " +
				"INDEX (b)" +
				")ENGINE=INNODB";
		stat.addBatch(statement);
		statement = "CREATE TABLE t2(" +
				"a int(10) NOT NULL, " +
				"b int(10), " +
				"PRIMARY KEY (a), " +
				//"FOREIGN KEY (b) REFERENCES t1 (b) ON DELETE CASCADE ON UPDATE CASCADE" +
				"FOREIGN KEY (b) REFERENCES t1 (b) ON DELETE RESTRICT on update cascade" +
				")ENGINE=INNODB";

		stat.addBatch(statement);
		//statement = "CREATE INDEX i1 ON t2 (b)";

		stat.executeBatch();
		connection.commit();
		stat.executeUpdate("insert into t1 values (1,1)");
		stat.executeUpdate("insert into t1 values (2,2)");
		stat.executeUpdate("insert into t1 values (3,3)");
		stat.executeUpdate("insert into t1 values (4,4)");
		stat.executeUpdate("insert into t1 values (5,5)");

		stat.executeUpdate("insert into t2 values (1,1)");
		stat.executeUpdate("insert into t2 values (2,2)");
		stat.executeUpdate("insert into t2 values (3,3)");
		stat.executeUpdate("insert into t2 values (4,4)");
		stat.executeUpdate("insert into t2 values (5,5)");

		connection.commit();
	}

}
