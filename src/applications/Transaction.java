package applications;


import java.sql.Connection;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface Transaction
{

	boolean executeTransaction(Connection con);
	boolean commitTransaction(Connection con);
	String getLastError();
	boolean isReadOnly();
	String getName();
}
