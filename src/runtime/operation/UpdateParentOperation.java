package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 11/05/15.
 */
public class UpdateParentOperation extends UpdateOperation implements ParentOperation
{
	private Map<ForeignKeyConstraint, List<Row>> childsByConstraint;
	private int numberRows;

	public UpdateParentOperation(ExecutionPolicy policy, Row updatedRow)
	{
		super(policy, updatedRow);
		this.childsByConstraint = new HashMap<>();
		this.numberRows = 0;
	}

	@Override
	public List<String> generateOperationStatements()
	{
		//TODO implement
		return null;
	}

	@Override
	public void addSideEffects(ForeignKeyConstraint constraint, List<Row> childs)
	{
		this.childsByConstraint.put(constraint, childs);
		this.numberRows += childs.size();
	}

	@Override
	public int getNumberOfRows()
	{
		return this.numberRows;
	}
}
