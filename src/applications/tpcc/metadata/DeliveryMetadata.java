package applications.tpcc.metadata;


/**
 * Created by dnlopes on 15/09/15.
 */
public class DeliveryMetadata
{

	private final int warehouseId;
	private final int carrierId;

	public DeliveryMetadata(int warehouseId, int carriedId)
	{
		this.warehouseId = warehouseId;
		this.carrierId = carriedId;
	}

	public int getWarehouseId()
	{
		return warehouseId;
	}

	public int getCarrierId()
	{
		return carrierId;
	}

}
