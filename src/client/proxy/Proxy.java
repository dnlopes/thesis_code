package client.proxy;


import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by dnlopes on 02/09/15.
 */
public interface Proxy
{
	int getProxyId();
	void setReadOnly(boolean readOnly);

	void abort();
	void commit() throws SQLException;
	void closeTransaction() throws SQLException;

	ResultSet executeQuery(String op) throws SQLException;
	int executeUpdate(String op) throws SQLException;
}
