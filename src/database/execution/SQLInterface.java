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

	public int executeUpdate(String sql) throws SQLException;
	public ResultSet executeQuery(String sql) throws SQLException;
	public int executeBatch() throws SQLException;
	public void addToBatchUpdate(String sql) throws SQLException;
	public Connection getConnection();
	public void commit() throws SQLException;
}
