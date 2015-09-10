package applications.tpcc;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface TpccConstants
{

	// transactions probabilities
	public static final int NEW_ORDER_TXN_RATE = 45;
	public static final int PAYMENT_TXN_RATE = 43;
	public static final int DELIVERY_TXN_RATE = 4;
	public static final int ORDER_STAT_TXN_RATE = 4;
	public static final int STOCK_LEVEL_TXN_RATE = 4;

	// general constants
	public static final int WAREHOUSES_NUMBER = 1;
	public static final int DISTRICTS_PER_WAREHOUSE = 10;
	public static final int CUSTOMER_PER_DISTRICT = 3000;
	public static int MAXITEMS = 100000;
	public static final boolean ALLOW_MULTI_WAREHOUSE_TX = true;



	// constants for NewOrder
	public static int MAX_NUM_ITEMS = 15;
	public static int MAX_ITEM_LEN = 24;
}
