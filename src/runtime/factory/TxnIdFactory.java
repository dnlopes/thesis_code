package runtime.factory;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by dnlopes on 11/03/15.
 */
public class TxnIdFactory
{

	private static TxnIdFactory ourInstance = new TxnIdFactory();
	private static final AtomicLong txnID = new AtomicLong();

	public static TxnIdFactory getInstance()
	{
		return ourInstance;
	}

	private TxnIdFactory()
	{
	}

	public static long getNextId()
	{
		return txnID.incrementAndGet();
	}
}
