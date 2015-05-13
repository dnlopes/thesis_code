package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteParentOperation extends DeleteOperation implements ParentOperation
{

	private Map<ForeignKeyConstraint, List<Row>> childsByConstraint;
	private int numberRows;

	public DeleteParentOperation(ExecutionPolicy policy, Row deletedRow)
	{
		super(policy, deletedRow);
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
	}

	@Override
	public int getNumberOfRows()
	{
		return this.numberRows;
	}
}
