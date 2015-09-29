package runtime.operation.crdt;


/**
 * Created by dnlopes on 18/09/15.
 */
public interface CRDTOperation
{

	public String getOperation();
	public String getClock();
	public String getTupleKey();

}
