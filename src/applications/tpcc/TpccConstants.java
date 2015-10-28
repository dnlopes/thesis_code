package applications.tpcc;


/**
 * Created by dnlopes on 05/09/15.
 */
public class TpccConstants
{

	// transactions probabilities
	// sum must be 100
	public static int NEW_ORDER_TXN_RATE = 100;		//45 is default
	public static int PAYMENT_TXN_RATE = 0;		//43 is default
	public static int DELIVERY_TXN_RATE = 0;	//4 is default

	public static int ORDER_STAT_TXN_RATE = 0;	//4 is default ; read only
	public static int STOCK_LEVEL_TXN_RATE = 0;	//4 is default ; read only

	// general constants
	public static int WAREHOUSES_NUMBER = 1;
	public static int DISTRICTS_PER_WAREHOUSE = 10;
	public static int CUSTOMER_PER_DISTRICT = 3000;
	public static int MAXITEMS = 100000;
	public static boolean ALLOW_MULTI_WAREHOUSE_TX = true;

	// constants for NewOrder
	public static int MAX_NUM_ITEMS = 15;
	public static int MAX_ITEM_LEN = 24;
}
