package applications.tpcc;


import applications.BaseBenchmarkOptions;
import applications.GeneratorUtils;
import applications.Transaction;
import applications.Workload;
import applications.tpcc.metadata.*;
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
	public Transaction getNextTransaction(BaseBenchmarkOptions options)
	{
		int random = GeneratorUtils.randomNumberIncludeBoundaries(1, 100);

		if(random <= TpccConstants.DELIVERY_TXN_RATE)
			return new DeliveryTransaction(createDeliveryMetadata(), options);
		else if(random <= TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE)
			return new NewOrderTransaction(createNewOrderMetadata(), options);
		else if(random <= TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants
				.ORDER_STAT_TXN_RATE)
			return new OrderStatTransaction(createOrderStatMetadata(), options);
		else if(random <= TpccConstants.DELIVERY_TXN_RATE + TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants
				.ORDER_STAT_TXN_RATE + TpccConstants.PAYMENT_TXN_RATE)
			return new PaymentTransaction(createPaymentMetadata(), options);
		else
			return new StockLevelTransaction(createStockLevelMetadata(), options);
	}

	@Override
	public float getWriteRate()
	{
		return (TpccConstants.NEW_ORDER_TXN_RATE + TpccConstants.DELIVERY_TXN_RATE + TpccConstants.PAYMENT_TXN_RATE) /
				100;
	}

	@Override
	public float getReadRate()
	{
		return (TpccConstants.ORDER_STAT_TXN_RATE + TpccConstants.STOCK_LEVEL_TXN_RATE) / 100;
	}

	@Override
	public float getCoordinatedOperationsRate()
	{
		return 0;
	}

	@Override
	public void addExtraColumns(StringBuilder buffer)
	{
		buffer.append(",neworder_rate,orderstat_rate,payment_rate,delivery_rate,stocklevel_rate");
	}

	@Override
	public void addExtraColumnValues(StringBuilder buffer)
	{
		buffer.append(",");
		buffer.append(TpccConstants.NEW_ORDER_TXN_RATE);
		buffer.append(",");
		buffer.append(TpccConstants.ORDER_STAT_TXN_RATE);
		buffer.append(",");
		buffer.append(TpccConstants.PAYMENT_TXN_RATE);
		buffer.append(",");
		buffer.append(TpccConstants.DELIVERY_TXN_RATE);
		buffer.append(",");
		buffer.append(TpccConstants.STOCK_LEVEL_TXN_RATE);
	}

	private static NewOrderMetadata createNewOrderMetadata()
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

	private static OrderStatMetadata createOrderStatMetadata()
	{
		int warehouseId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER);
		int districtId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.DISTRICTS_PER_WAREHOUSE);
		int customerId = GeneratorUtils.nuRand(1023, 1, TpccConstants.CUSTOMER_PER_DISTRICT);
		String c_last = GeneratorUtils.lastName(GeneratorUtils.nuRand(255, 0, 999));
		int byname = 0;

		if(GeneratorUtils.randomNumber(1, 100) <= 60)
		{
			byname = 1; /* select by last name */
		} else
		{
			byname = 0; /* select by customer id */
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

		return new OrderStatMetadata(warehouseId, districtId, customerId, byname, c_last);
	}

	private static DeliveryMetadata createDeliveryMetadata()
	{
		int warehouseId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER);
		int carrierId = GeneratorUtils.randomNumberIncludeBoundaries(1, 10);

		if(warehouseId < 1 || warehouseId > TpccConstants.WAREHOUSES_NUMBER)
		{
			LOG.error("invalid warehouse id: {}", warehouseId);
			return null;
		}

		return new DeliveryMetadata(warehouseId, carrierId);
	}

	private static PaymentMetadata createPaymentMetadata()
	{
		int warehouseId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER);
		int districtId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.DISTRICTS_PER_WAREHOUSE);
		int customerId = GeneratorUtils.nuRand(1023, 1, TpccConstants.CUSTOMER_PER_DISTRICT);
		String c_last = GeneratorUtils.lastName(GeneratorUtils.nuRand(255, 0, 999));
		int h_amount = GeneratorUtils.randomNumberIncludeBoundaries(1, 5000);

		int c_w_id, c_d_id;
		int byname;

		if(GeneratorUtils.randomNumber(1, 100) <= 60)
		{
			byname = 1; /* select by last name */
		} else
		{
			byname = 0; /* select by customer id */
		}

		if(TpccConstants.ALLOW_MULTI_WAREHOUSE_TX)
		{
			if(GeneratorUtils.randomNumberIncludeBoundaries(1, 100) <= 85)
			{
				c_w_id = warehouseId;
				c_d_id = districtId;
			} else
			{
				c_w_id = selectRemoteWarehouse(warehouseId);
				c_d_id = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.DISTRICTS_PER_WAREHOUSE);
			}
		} else
		{
			c_w_id = warehouseId;
			c_d_id = districtId;
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

		return new PaymentMetadata(warehouseId, c_w_id, districtId, c_d_id, customerId, byname, h_amount, c_last);

	}

	private static StockLevelMetadata createStockLevelMetadata()
	{

		int warehouseId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.WAREHOUSES_NUMBER);
		int districtId = GeneratorUtils.randomNumberIncludeBoundaries(1, TpccConstants.DISTRICTS_PER_WAREHOUSE);
		int level = GeneratorUtils.randomNumberIncludeBoundaries(10, 20);

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

		return new StockLevelMetadata(warehouseId, districtId, level);
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
