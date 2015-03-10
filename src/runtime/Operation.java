package runtime;

/**
 * Created by dnlopes on 10/03/15.
 */
public interface Operation
{

	public void clear();
	public void addOperationEntry(/*OpEntry entry*/);
	public boolean executeOperation();

}
