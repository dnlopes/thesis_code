package runtime.factory;

/**
 * Created by dnlopes on 11/03/15.
 */
public class TxnIdFactory
{

	private static TxnIdFactory ourInstance = new TxnIdFactory();
	private static long txnID = 0;

	public static TxnIdFactory getInstance()
	{
		return ourInstance;
	}

	private TxnIdFactory()
	{
	}

	public synchronized static long getNextId()
	{
		return ++ txnID;
	}
}
