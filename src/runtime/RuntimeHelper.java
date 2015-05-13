package runtime;

/**
 * Created by dnlopes on 13/03/15.
 */
public class RuntimeHelper
{

	public static void throwRunTimeException(String message, int exitCode)
	{
		try
		{
			throw new RuntimeException(message);
		} catch(RuntimeException e)
		{
			e.printStackTrace();
			System.exit(exitCode);
		}
	}
}
