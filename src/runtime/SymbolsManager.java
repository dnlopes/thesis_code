package runtime;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by dnlopes on 22/10/15.
 */
public class SymbolsManager
{
	public static final String SYMBOL_PREFIX = "_SYM_";

	private static final SymbolsManager ourInstance = new SymbolsManager();
	private static AtomicInteger symbolCounter;


	private SymbolsManager()
	{
		symbolCounter = new AtomicInteger();
	}

	public static SymbolsManager getInstance()
	{
		return ourInstance;
	}


	public static String getNextSymbol()
	{
		int symbolNumber = symbolCounter.incrementAndGet();

		return SYMBOL_PREFIX + String.valueOf(symbolNumber);
	}

}
