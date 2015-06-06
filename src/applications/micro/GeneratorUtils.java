package applications.micro;


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

	public static int randomNumber(int min, int max)
	{
		int next = RANDOM.nextInt();
		int div = next % ((max - min) + 1);

		if(div < 0)
		{
			div = div * -1;
		}
		return min + div;

	}
	
}
