package runtime.txn;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runtime.RuntimeHelper;
import util.ExitCode;


/**
 * Created by dnlopes on 17/03/15.
 */
public class TransactionIdentifier
{

	private static final Logger LOG = LoggerFactory.getLogger(TransactionIdentifier.class);

	public static final int DEFAULT_VALUE = -1;
	private int id;

	public TransactionIdentifier()
	{
		this.id = DEFAULT_VALUE;
	}

	public TransactionIdentifier(int id)
	{
		this.id = id;
	}

	public int getValue()
	{
		return this.id;
	}

	public void setValue(int id)
	{
		if(this.id != DEFAULT_VALUE)
		{
			LOG.error("concurrency problem. Go check it out");
			RuntimeHelper.throwRunTimeException("concurrent modification to id", ExitCode.INVALIDUSAGE);
		}

		this.id = id;
	}

	public void resetValue()
	{
		this.id = DEFAULT_VALUE;
	}
}
