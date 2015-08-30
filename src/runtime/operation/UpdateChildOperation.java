package runtime.operation;


import database.constraints.fk.ForeignKeyConstraint;
import database.util.ExecutionPolicy;
import database.util.value.FieldValue;
import database.util.Row;
import runtime.transformer.OperationTransformer;
import util.defaults.DBDefaults;
import util.thrift.RequestValue;
import util.thrift.ThriftShadowTransaction;

import java.util.Map;


/**
 * Created by dnlopes on 23/06/15.
 */
public class UpdateChildOperation extends UpdateOperation
{

	//@TODO: we must implement the logic where the child tuple "changes" parent
	private Map<ForeignKeyConstraint, Row> parentRows;
	
	public UpdateChildOperation(int id, ExecutionPolicy policy, Row updatedRow, Map<ForeignKeyConstraint, Row> parents)
	{
		super(id, policy, updatedRow);
		this.parentRows = parents;
	}

	@Override
	public void generateStatements(ThriftShadowTransaction shadowTransaction)
	{
		//done
		this.row.updateFieldValue(
				new FieldValue(this.row.getTable().getContentClockField(), DBDefaults.CLOCK_VALUE_PLACEHOLDER));

		this.row.mergeUpdates();

		String updateStatement = OperationTransformer.generateUpdateStatement(this.row);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), updateStatement);

		String mergeClockStatement = OperationTransformer.mergeContentClock(this.row);
		shadowTransaction.putToOperations(shadowTransaction.getOperationsSize(), mergeClockStatement);

		if(!this.isFinal)
		{
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), updateStatement);

			for(RequestValue rValue : this.requestValues)
				rValue.setOpId(shadowTransaction.getOperations().size());
		}

		if(this.tablePolicy == ExecutionPolicy.UPDATEWINS)
		{
			String insertRowBack = OperationTransformer.generateInsertParentRowBack(this.row, this.parentRows);
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), insertRowBack);
			mergeClockStatement = OperationTransformer.mergeDeletedClock(this.row);
			shadowTransaction.putToTempOperations(shadowTransaction.getOperationsSize(), mergeClockStatement);
		}
	}
}

