package applications;


import java.sql.Connection;
import java.sql.SQLException;


/**
 * Created by dnlopes on 02/12/15.
 */
public abstract class AbstractTransaction implements Transaction
{

	protected String lastError;
	protected String txnName;

	public AbstractTransaction(String name)
	{
		this.txnName = name;
	}

	@Override
	public String getLastError()
	{
		return this.lastError;
	}

	@Override
	public boolean commitTransaction(Connection con)
	{
		try
		{
			con.commit();
			return true;

		} catch(SQLException e)
		{
			lastError = e.getMessage();
			this.rollbackQuietly(con);
			return false;
		}
	}

	protected void rollbackQuietly(Connection connection)
	{
		try
		{
			connection.rollback();
		} catch(SQLException ignored)
		{

		}
	}

	public String getName()
	{
		return this.txnName;
	}
}
