package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.Row;
import util.thrift.RequestValue;
import util.thrift.ThriftShadowTransaction;

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

	public UpdateParentOperation(int id, ExecutionPolicy policy, Row updatedRow)
	{
		super(id, policy, updatedRow);
		this.childsByConstraint = new HashMap<>();
		this.numberRows = 0;
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		super.generateStatements(shadowTransaction);

		if(this.row.hasSideEffects())
		{
			for(Map.Entry<ForeignKeyConstraint, List<Row>> entry : this.childsByConstraint.entrySet())
			{
				StringBuilder buffer = new StringBuilder();
				buffer.append("UPDATE ");
				buffer.append(entry.getKey().getChildTable().getName());
				buffer.append(" SET ");
			//TODO
			}
		}

		                /*
		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), op);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperations().size());
		}       */
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
