package runtime.operation;


import database.util.ExecutionPolicy;

import java.util.List;


/**
 * Created by dnlopes on 11/05/15.
 */
public interface Operation
{
	public List<String> generateOperationStatements();
	public ExecutionPolicy getTablePolicy();
	public OperationType getOpType();
}
