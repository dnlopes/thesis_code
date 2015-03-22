package runtime;


/**
 * Created by dnlopes on 17/03/15.
 */
public class TransactionId
{

	private long id;

	public TransactionId(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}
}
