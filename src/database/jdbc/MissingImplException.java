package database.jdbc;

import java.sql.SQLException;


/**
 * Created by dnlopes on 11/03/15.
 */
public class MissingImplException extends SQLException
{


	public MissingImplException()
	{
	}

	public MissingImplException(String arg0)
	{
		super(arg0);
	}

	public MissingImplException(Throwable arg0)
	{
		super(arg0);
	}

}
