package applications.tpcc;


import applications.GeneratorUtils;
import applications.Transaction;
import applications.Workload;
import applications.tpcc.txn.*;


/**
 * Created by dnlopes on 05/09/15.
 */
public class TpccWorkload implements Workload
{
	
	@Override
	public String getNextOperation()
	{
		return null;
	}

	@Override
	public int getWriteRate()
	{
		return 0;
	}

	@Override
	public int getCoordinatedRate()
	{
		return 0;
	}

	@Override
	public Transaction getNextTransaction()
	{
		int random = GeneratorUtils.randomNumberIncludeBoundaries(0, 100);

		if(random < TpccConstants.DELIVERY_TXN_RATE)
			return new DeliveryTransaction();
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE)
			return new NewOrderTransaction();
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants.ORDER_STAT_TXN_RATE)
			return new OrderStatTransaction();
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants
				.ORDER_STAT_TXN_RATE + TpccConstants.PAYMENT_TXN_RATE)
			return new PaymentTransaction();
		else
			return new SlevTransaction();
	}
}
