package applications.tpcc;


import applications.GeneratorUtils;
import applications.Transaction;
import applications.Workload;
import applications.tpcc.metadata.NewOrderMetadata;
import applications.tpcc.txn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by dnlopes on 05/09/15.
 */
public class TpccWorkload implements Workload
{

	private static final Logger LOG = LoggerFactory.getLogger(TpccWorkload.class);

	@Override
	public Transaction getNextTransaction()
	{
		int random = GeneratorUtils.randomNumberIncludeBoundaries(0, 100);

		if(random < TpccConstants.DELIVERY_TXN_RATE)
			return new DeliveryTransaction();
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE)
			return new NewOrderTransaction(createNewOrderMetadata());
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants
				.ORDER_STAT_TXN_RATE)
			return new OrderStatTransaction();
		else if(random < TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants
				.ORDER_STAT_TXN_RATE + TpccConstants.PAYMENT_TXN_RATE)
			return new PaymentTransaction();
		else
			return new StockLevelTransaction();
	}

	public static NewOrderMetadata createNewOrderMetadata()
	{
		int warehouseId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER);
		int districtId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.DISTRICTS_PER_WAREHOUSE);
		int customerId = GeneratorUtils.nuRand(1023, 1, TpccConstants.CUSTOMER_PER_DISTRICT);
		int orderLinesNumber = GeneratorUtils.randomNumberIncludeBoundaries(5, 15);
		int rbk = GeneratorUtils.randomNumberIncludeBoundaries(1, 100);
		int all_local = 0;

		int[] itemsIds = new int[TpccConstants.MAX_NUM_ITEMS];
		int[] supplierWarehouseIds = new int[TpccConstants.MAX_NUM_ITEMS];
		int[] quantities = new int[TpccConstants.MAX_NUM_ITEMS];

		int notfound = TpccConstants.MAXITEMS + 1;

		for(int i = 0; i < orderLinesNumber; i++)
		{
			itemsIds[i] = GeneratorUtils.nuRand(8191, 1, TpccConstants.MAXITEMS);
			if((i == orderLinesNumber - 1) && (rbk == 1))
			{
				itemsIds[i] = notfound;
			}
			if(TpccConstants.ALLOW_MULTI_WAREHOUSE_TX)
			{
				if(GeneratorUtils.randomNumberIncludeBoundaries(1, 100) != 1)
				{
					supplierWarehouseIds[i] = warehouseId;
				} else
				{
					supplierWarehouseIds[i] = selectRemoteWarehouse(warehouseId);
					all_local = 0;
				}
			} else
			{
				supplierWarehouseIds[i] = warehouseId;
			}
			quantities[i] = GeneratorUtils.randomNumberIncludeBoundaries(1, 10);
		}

		if(warehouseId < 1 || warehouseId > TpccConstants.WAREHOUSES_NUMBER)
		{
			LOG.error("invalid warehouse id: {}", warehouseId);
			return null;
		}

		if(districtId < 1 || districtId > TpccConstants.DISTRICTS_PER_WAREHOUSE)
		{
			LOG.error("invalid district id: {}", districtId);
			return null;
		}

		if(customerId < 1 || customerId > TpccConstants.CUSTOMER_PER_DISTRICT)
		{
			LOG.error("invalid customer id: {}", customerId);
			return null;
		}

		return new NewOrderMetadata(warehouseId, districtId, customerId, orderLinesNumber, all_local, itemsIds,
				supplierWarehouseIds, quantities);
	}

	private static int selectRemoteWarehouse(int home_ware)
	{
		int tmp;

		if(TpccConstants.WAREHOUSES_NUMBER == 1)
			return home_ware;
		while((tmp = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER)) == home_ware)
			;
		return tmp;
	}

}
