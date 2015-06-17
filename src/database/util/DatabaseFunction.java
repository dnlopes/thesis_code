package database.util;


import java.text.DateFormat;
import java.util.Date;


public class DatabaseFunction
{

	public static Date NOW()
	{
		return new Date();
	}

	public static String CURRENTTIMESTAMP(DateFormat dateFormat)
	{
		return dateFormat.format(NOW());
	}
	
}