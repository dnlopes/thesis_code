package applications.tpcc;


/**
 * Created by dnlopes on 05/09/15.
 */
public interface TpccConstants
{

	// transactions probabilities
	// sum must be 100
	int NEW_ORDER_TXN_RATE = 100;		//45 is default
	int PAYMENT_TXN_RATE = 0;		//43 is default
	int DELIVERY_TXN_RATE = 0;	//4 is default

	int ORDER_STAT_TXN_RATE = 0;	//4 is default ; read only
	int STOCK_LEVEL_TXN_RATE = 0;	//4 is default ; read only

	// general constants
	int WAREHOUSES_NUMBER = 1;
	int DISTRICTS_PER_WAREHOUSE = 10;
	int CUSTOMER_PER_DISTRICT = 3000;
	int MAXITEMS = 100000;
	boolean ALLOW_MULTI_WAREHOUSE_TX = true;

	// constants for NewOrder
	int MAX_NUM_ITEMS = 15;
	int MAX_ITEM_LEN = 24;
}
