package applications;


import java.sql.Connection;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface Transaction
{

	public boolean executeTransaction(Connection con);
	public String getLastError();
	public boolean isReadOnly();
	public String getName();
}
