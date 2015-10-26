package database.execution;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 25/09/15.
 * * A thin class that allows direct interaction with the database.
 */
public interface SQLInterface
{

	int executeUpdate(String sql) throws SQLException;
	ResultSet executeQuery(String sql) throws SQLException;
	int executeBatch() throws SQLException;
	void addToBatchUpdate(String sql) throws SQLException;
	Connection getConnection();
	void commit() throws SQLException;
}
