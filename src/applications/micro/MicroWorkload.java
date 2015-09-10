package applications.micro;


import applications.Transaction;
import applications.Workload;


/**
 * Created by dnlopes on 05/06/15.
 */
public class MicroWorkload implements Workload
{

	public MicroWorkload()
	{
	}

	@Override
	public Transaction getNextTransaction()
	{
		return null;
	}

}
