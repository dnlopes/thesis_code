package applications.tpcc.metadata;


/**
 * Created by dnlopes on 15/09/15.
 */
public class OrderStatMetadata
{

	private final int warehouseId;
	private final int districtId;
	private final int customerId;
	private final int byname;

	private final String lastName;

	public OrderStatMetadata(int warehouseId, int districtId, int customerId, int byname, String lastName)
	{
		this.warehouseId = warehouseId;
		this.districtId = districtId;
		this.customerId = customerId;
		this.byname = byname;
		this.lastName = lastName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public int getByname()
	{
		return byname;
	}

	public int getCustomerId()
	{
		return customerId;
	}

	public int getDistrictId()
	{
		return districtId;
	}

	public int getWarehouseId()
	{
		return warehouseId;
	}

}
