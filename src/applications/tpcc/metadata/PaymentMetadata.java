package applications.tpcc.metadata;


/**
 * Created by dnlopes on 15/09/15.
 */
public class PaymentMetadata
{

	private final int warehouseId;
	private final int customerWarehouseId;
	private final int districtId;
	private final int customerDistrictId;
	private final int customerId;
	private final int byname;
	private final int h_amount;
	private final String lastName;

	public PaymentMetadata(int warehouseId, int customerWarehouseId, int districtId, int customerDistrictId,
						   int customerId, int byname, int h_amount, String lastName)
	{
		this.warehouseId = warehouseId;
		this.customerWarehouseId = customerWarehouseId;
		this.districtId = districtId;
		this.customerDistrictId = customerDistrictId;
		this.customerId = customerId;
		this.byname = byname;
		this.lastName = lastName;
		this.h_amount = h_amount;
	}


	public int getWarehouseId()
	{
		return warehouseId;
	}

	public int getCustomerWarehouseId()
	{
		return customerWarehouseId;
	}

	public int getDistrictId()
	{
		return districtId;
	}

	public int getCustomerDistrictId()
	{
		return customerDistrictId;
	}

	public int getCustomerId()
	{
		return customerId;
	}

	public int getByname()
	{
		return byname;
	}

	public int getH_amount()
	{
		return h_amount;
	}

	public String getLastName()
	{
		return lastName;
	}

}
