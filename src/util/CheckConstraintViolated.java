package util;


import java.sql.SQLException;


/**
 * Created by dnlopes on 23/03/15.
 */
public class CheckConstraintViolated extends SQLException
{

	public CheckConstraintViolated()
	{
		super("check constraint violated");
	}

	public CheckConstraintViolated(String arg0)
	{
		super(arg0);
	}

	public CheckConstraintViolated(Throwable arg0)
	{
		super(arg0);
	}


}