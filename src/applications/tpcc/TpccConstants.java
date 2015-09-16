package applications.tpcc;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface TpccConstants
{

	// transactions probabilities
	// sum must be 100
	public static int NEW_ORDER_TXN_RATE = 20;		//45 is default
	public static int PAYMENT_TXN_RATE = 0;		//43 is default
	public static int DELIVERY_TXN_RATE = 0;	//4 is default

	public static int ORDER_STAT_TXN_RATE = 80;	//4 is default ; read only
	public static int STOCK_LEVEL_TXN_RATE = 0;	//4 is default ; read only

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
