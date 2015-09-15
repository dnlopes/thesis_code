package applications.tpcc.metadata;


/**
 * Created by dnlopes on 15/09/15.
 */
public class StockLevelMetadata
{

	private final int warehouseId;
	private final int districtId;
	private final int level;

	public StockLevelMetadata(int warehouseId, int districtId, int level)
	{
		this.warehouseId = warehouseId;
		this.districtId = districtId;
		this.level = level;
	}

	public int getWarehouseId()
	{
		return warehouseId;
	}

	public int getLevel()
	{
		return level;
	}

	public int getDistrictId()
	{
		return districtId;
	}

}
