package runtime.operation;


import database.constraints.fk.ForeignKeyAction;
import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.Row;
import runtime.RuntimeUtils;
import runtime.transformer.OperationTransformer;
import runtime.transformer.QueryCreator;
import util.ExitCode;
import util.thrift.ThriftShadowTransaction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by dnlopes on 13/05/15.
 */
public class DeleteParentOperation extends DeleteOperation implements ParentOperation
{

	private Map<ForeignKeyConstraint, List<Row>> childsByConstraint;
	private int numberRows;
	private List<Row> childs;

	public DeleteParentOperation(int id, ExecutionPolicy policy, Row deletedRow)
	{
		super(id, policy, deletedRow);
		this.childsByConstraint = new HashMap<>();
		this.numberRows = 0;
		this.childs = new LinkedList<>();
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		// delete parent row
		super.generateStatements(shadowTransaction);

		// delete childs
		for(Map.Entry<ForeignKeyConstraint, List<Row>> entry : this.childsByConstraint.entrySet())
		{
			ForeignKeyConstraint fkConstraint = entry.getKey();

			String deleteChilds = OperationTransformer.generateDeleteChilds(fkConstraint, entry.getValue());
			shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), deleteChilds);

			if(fkConstraint.getPolicy().getDeleteAction() == ForeignKeyAction.CASCADE)
			{
				if(fkConstraint.getPolicy().getExecutionPolicy() == ExecutionPolicy.DELETEWINS)
				{
					String deleteConcurrentChilds = OperationTransformer.generateDeleteConcurrentChilds(this.row,
							fkConstraint);
					shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), deleteConcurrentChilds);
				}
			} else if(fkConstraint.getPolicy().getDeleteAction() == ForeignKeyAction.SET_NULL)
				RuntimeUtils.throwRunTimeException("set null side effect noy yet supported",
						ExitCode.MISSING_IMPLEMENTATION);
		}
	}

	@Override
	public void addSideEffects(ForeignKeyConstraint constraint, List<Row> childs)
	{
		this.childsByConstraint.put(constraint, childs);
		this.childs.addAll(childs);
	}

	@Override
	public int getNumberOfRows()
	{
		return this.numberRows;
	}

}
