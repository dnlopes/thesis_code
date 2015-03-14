package runtime;

import java.util.Objects;


/**
 * Created by dnlopes on 10/03/15.
 */
public abstract class Operation
{
	public Operation op;
	public String[] pk;

	//public void clear();
	//public void addOperationEntry(/*OpEntry entry*/);
	//public boolean executeOperation();
	public abstract Object getStatementObj();
	public abstract Object getStatement();
	public abstract Object executeDefOpUpdate();
	public abstract Object executeDefOpDelete();
	public abstract Objects executeDefOpInsert();

}
