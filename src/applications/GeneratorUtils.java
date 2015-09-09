package applications;


import java.util.Random;


/**
 * Created by dnlopes on 05/06/15.
 */
public class GeneratorUtils
{

	private static final Random RANDOM = new Random(System.nanoTime());

	public static String makeAlphaString(int x, int y)
	{
		String str = null;
		String temp = "0123456789" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";
		char[] alphanum = temp.toCharArray();
		int arrmax = 61;  /* index of last array element */
		int i;
		int len;
		len = randomNumber(x, y);

		for(i = 0; i < len; i++)
			if(str != null)
			{
				str = str + alphanum[randomNumber(0, arrmax)];
			} else
			{
				str = "" + alphanum[randomNumber(0, arrmax)];
			}

		return str;

	}

	/**
	 *
	 * @param min
	 * @param max
	 * @return returns a value between min (inclusive) and max (exclusive)
	 */
	public static int randomNumber(int min, int max)
	{
		return RANDOM.nextInt(max-min) + min;
	}

	/**
	 *
	 * @param min
	 * @param max
	 * @return returns a value between min (inclusive) and max (inclusive)
	 */
	public static int randomNumberIncludeBoundaries(int min, int max)
	{
		return randomNumber(min, max+1);
	}
}
