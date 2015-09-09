package applications;


import java.sql.Connection;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface Transaction
{

	public boolean executeTransaction(Connection con);
	public boolean isReadOnly();
}
