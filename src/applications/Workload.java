package applications;

/**
 * Created by dnlopes on 05/06/15.
 */
public interface Workload
{
	public String getNextOperation();
	public int getWriteRate();
	public int getCoordinatedRate();

	public Transaction getNextTransaction();
}
