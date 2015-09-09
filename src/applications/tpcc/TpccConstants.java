package applications.tpcc;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface TpccConstants
{

	// maximum value allowed: 43
	public static final int NEW_ORDER_TXN_RATE = 43;

	// the sum of the next for entries must be less then 57
	public static final int PAYMENT_TXN_RATE = 10;
	public static final int DELIVERY_TXN_RATE = 17;
	public static final int ORDER_STAT_TXN_RATE = 15;
	public static final int SLEV_TXN_RATE = 15;

	// the sum of all txn rate must be 100
}
