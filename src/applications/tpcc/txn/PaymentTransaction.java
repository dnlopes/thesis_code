package applications.tpcc.txn;


import applications.Transaction;

import java.sql.Connection;


/**
 * Created by dnlopes on 05/09/15.
 */
public class PaymentTransaction implements Transaction
{
	
	@Override
	public boolean executeTransaction(Connection con)
	{
		return false;
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}
}
