package util.exception;


/**
 * Created by dnlopes on 28/03/15.
 */
public class ConfigurationLoadException extends Exception
{
	public ConfigurationLoadException()
	{
		super("check constraint violated");
	}

	public ConfigurationLoadException(String arg0)
	{
		super(arg0);
	}

	public ConfigurationLoadException(Throwable arg0)
	{
		super(arg0);
	}
	
}
