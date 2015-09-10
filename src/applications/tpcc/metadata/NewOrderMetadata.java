package applications.tpcc.metadata;


/**
 * Created by dnlopes on 10/09/15.
 */
public final class NewOrderMetadata
{

	private final int warehouseId;
	private final int districtId;
	private final int customerId;
	private final int numberOfItems;
	private final int allOrderLinesLocal;
	private final int[] itemsIdsArray;
	private final int[] warehouseSuplierItems;
	private final int[] quantities;

	public NewOrderMetadata(int warehouseId, int districtId, int customerId, int numberOfItems, int allOrderLinesLocal,
							int[] itemsIdsArray, int[] warehouseSuplierItems, int[] quantities)
	{
		this.warehouseId = warehouseId;
		this.districtId = districtId;
		this.customerId = customerId;
		this.numberOfItems = numberOfItems;
		this.allOrderLinesLocal = allOrderLinesLocal;
		this.itemsIdsArray = itemsIdsArray;
		this.warehouseSuplierItems = warehouseSuplierItems;
		this.quantities = quantities;

	}

	public int getWarehouseId()
	{
		return warehouseId;
	}

	public int getDistrictId()
	{
		return districtId;
	}

	public int getCustomerId()
	{
		return customerId;
	}

	public int getNumberOfItems()
	{
		return numberOfItems;
	}

	public int getAllOrderLinesLocal()
	{
		return allOrderLinesLocal;
	}

	public int[] getItemsIdsArray()
	{
		return itemsIdsArray;
	}

	public int[] getWarehouseSuplierItems()
	{
		return warehouseSuplierItems;
	}

	public int[] getQuantities()
	{
		return quantities;
	}

}
