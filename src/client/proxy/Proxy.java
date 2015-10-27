package client.proxy;


import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 02/09/15.
 */
public interface Proxy
{
	public int assignConnectionId();
	public void setReadOnly(boolean readOnly);

	public void abort(int connectionId);
	public void commit(int connectionId) throws SQLException;
	public void closeTransaction(int connectionId) throws SQLException;

	public ResultSet executeQuery(String op, int connectionId) throws SQLException;
	public int executeUpdate(String op, int connectionId) throws SQLException;




}
